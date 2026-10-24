import type {
  Block, Expr, MethodDecl, Pos, Stmt,
} from "./ast";
import { RuntimeError, StopSignal } from "./errors";
import type { Controller, IO } from "./io";
import {
  asBoolean, coerceTo, defaultValue, display, isNumeric, jbool, jchar,
  jdouble, jint, jlong, jstr, NULL, num, type JValue,
} from "./values";

// ---- control-flow signals -------------------------------------------------
class BreakSignal { }
class ContinueSignal { }
class ReturnSignal { constructor(public value: JValue) { } }

// ---- variable scopes ------------------------------------------------------
class Environment {
  private vars = new Map<string, JValue>();
  constructor(public parent: Environment | null) { }
  define(name: string, value: JValue): void { this.vars.set(name, value); }
  has(name: string): boolean { return this.vars.has(name) || (this.parent?.has(name) ?? false); }
  get(name: string): JValue | undefined {
    return this.vars.has(name) ? this.vars.get(name) : this.parent?.get(name);
  }
  assign(name: string, value: JValue): boolean {
    if (this.vars.has(name)) { this.vars.set(name, value); return true; }
    return this.parent?.assign(name, value) ?? false;
  }
}

const STATIC_CLASSES = new Set(["System", "Math", "Integer", "Double", "Long", "Boolean", "Character", "String", "Arrays"]);

interface ScannerState { buffer: string[]; io: IO; }

export class Interpreter {
  private methods = new Map<string, MethodDecl>();
  private steps = 0;
  private readonly maxSteps = 80_000_000;

  constructor(methods: MethodDecl[], private io: IO, private ctrl: Controller) {
    for (const m of methods) this.methods.set(m.name, m);
  }

  async run(): Promise<void> {
    const main = this.methods.get("main");
    if (!main) {
      throw new RuntimeError(
        "Es gibt keine main-Methode. Dein Programm braucht\n    public static void main(String[] args)\num starten zu können.", null);
    }
    const env = new Environment(null);
    if (main.params.length > 0) env.define(main.params[0]!.name, { t: "array", elem: "String", v: [] });
    try {
      await this.execBlock(main.body, env);
    } catch (e) {
      if (e instanceof ReturnSignal) return;
      throw e;
    }
  }

  // ---- statements ---------------------------------------------------------
  private async execBlock(block: Block, parent: Environment): Promise<void> {
    const env = new Environment(parent);
    for (const s of block.stmts) await this.execStmt(s, env);
  }

  private async execStmt(stmt: Stmt, env: Environment): Promise<void> {
    await this.checkpoint(stmt.pos);
    switch (stmt.kind) {
      case "block": return this.execBlock(stmt, env);
      case "empty": return;
      case "exprstmt": await this.evalExpr(stmt.expr, env); return;
      case "var": {
        for (const d of stmt.decls) {
          let value: JValue;
          if (d.init) value = await this.evalInit(d.init, stmt.type.base, stmt.type.dims, env);
          else value = defaultValue(stmt.type.base, stmt.type.dims);
          if (stmt.type.base !== "var" && stmt.type.dims === 0 && d.init) value = coerceTo(stmt.type.base, value);
          env.define(d.name, value);
        }
        return;
      }
      case "if":
        if (asBoolean(await this.condition(stmt.cond, env))) await this.execStmt(stmt.then, env);
        else if (stmt.otherwise) await this.execStmt(stmt.otherwise, env);
        return;
      case "while":
        while (asBoolean(await this.condition(stmt.cond, env))) {
          await this.checkpoint(stmt.pos);
          try { await this.execStmt(stmt.body, env); }
          catch (e) { if (e instanceof BreakSignal) break; if (e instanceof ContinueSignal) continue; throw e; }
        }
        return;
      case "do":
        do {
          await this.checkpoint(stmt.pos);
          try { await this.execStmt(stmt.body, env); }
          catch (e) { if (e instanceof BreakSignal) break; if (e instanceof ContinueSignal) continue; throw e; }
        } while (asBoolean(await this.condition(stmt.cond, env)));
        return;
      case "for": {
        const loopEnv = new Environment(env);
        if (stmt.init) await this.execStmt(stmt.init, loopEnv);
        while (stmt.cond === null || asBoolean(await this.condition(stmt.cond, loopEnv))) {
          await this.checkpoint(stmt.pos);
          try { await this.execStmt(stmt.body, loopEnv); }
          catch (e) {
            if (e instanceof BreakSignal) break;
            if (!(e instanceof ContinueSignal)) throw e;
          }
          for (const u of stmt.update) await this.evalExpr(u, loopEnv);
        }
        return;
      }
      case "foreach": {
        const iter = await this.evalExpr(stmt.iterable, env);
        if (iter.t !== "array") throw new RuntimeError("Mit for-each kann man nur über Arrays/Listen gehen.", stmt.pos);
        for (const item of iter.v) {
          await this.checkpoint(stmt.pos);
          const loopEnv = new Environment(env);
          loopEnv.define(stmt.name, item);
          try { await this.execStmt(stmt.body, loopEnv); }
          catch (e) { if (e instanceof BreakSignal) break; if (e instanceof ContinueSignal) continue; throw e; }
        }
        return;
      }
      case "break": throw new BreakSignal();
      case "continue": throw new ContinueSignal();
      case "return": throw new ReturnSignal(stmt.value ? await this.evalExpr(stmt.value, env) : NULL);
    }
  }

  private async condition(expr: Expr, env: Environment): Promise<JValue> {
    const v = await this.evalExpr(expr, env);
    if (v.t !== "boolean") throw new RuntimeError("Eine Bedingung muss true oder false ergeben (boolean).", expr.pos);
    return v;
  }

  private async evalInit(init: Expr, base: string, dims: number, env: Environment): Promise<JValue> {
    if (init.kind === "arrayinit") {
      const elems: JValue[] = [];
      for (const e of init.elements) elems.push(dims > 1 ? await this.evalExpr(e, env) : coerceTo(base, await this.evalExpr(e, env)));
      return { t: "array", elem: base, v: elems };
    }
    return this.evalExpr(init, env);
  }

  // ---- expressions --------------------------------------------------------
  private async evalExpr(expr: Expr, env: Environment): Promise<JValue> {
    switch (expr.kind) {
      case "lit": return this.literal(expr.value, expr.type);
      case "name": {
        const v = env.get(expr.name);
        if (v === undefined) throw new RuntimeError(`Die Variable '${expr.name}' ist hier nicht bekannt. Tippfehler oder nicht deklariert?`, expr.pos);
        return v;
      }
      case "arrayinit": {
        const elems: JValue[] = [];
        for (const e of expr.elements) elems.push(await this.evalExpr(e, env));
        return { t: "array", elem: elems[0]?.t ?? "Object", v: elems };
      }
      case "logical": {
        const l = await this.evalExpr(expr.left, env);
        if (expr.op === "&&") return asBoolean(l) ? await this.evalExpr(expr.right, env) : jbool(false);
        return asBoolean(l) ? jbool(true) : await this.evalExpr(expr.right, env);
      }
      case "bin": return this.binary(expr.op, await this.evalExpr(expr.left, env), await this.evalExpr(expr.right, env), expr.pos);
      case "unary": return this.unary(expr.op, await this.evalExpr(expr.operand, env), expr.pos);
      case "ternary": return asBoolean(await this.condition(expr.cond, env)) ? this.evalExpr(expr.then, env) : this.evalExpr(expr.otherwise, env);
      case "assign": return this.assign(expr.target, expr.op, expr.value, env);
      case "update": return this.update(expr.operand, expr.op, expr.prefix, env, expr.pos);
      case "index": {
        const arr = await this.evalExpr(expr.array, env);
        const idx = await this.evalExpr(expr.index, env);
        if (arr.t !== "array") throw new RuntimeError("Hier wird ein Array erwartet.", expr.pos);
        const i = num(idx);
        if (i < 0 || i >= arr.v.length) throw new RuntimeError(`Array-Index ${i} ist außerhalb (Länge ${arr.v.length}).`, expr.pos);
        return arr.v[i]!;
      }
      case "field": return this.fieldAccess(expr, env);
      case "call": return this.call(expr, env);
      case "newarray": return this.newArray(expr, env);
      case "new": return this.newObject(expr, env);
    }
  }

  private literal(value: unknown, type: string): JValue {
    switch (type) {
      case "int": return jint(value as number);
      case "long": return jlong(value as number);
      case "double": return jdouble(value as number);
      case "char": return jchar((value as string).codePointAt(0) ?? 0);
      case "boolean": return jbool(value as boolean);
      case "String": return jstr(value as string);
      default: return NULL;
    }
  }

  private binary(op: string, a: JValue, b: JValue, pos: Pos): JValue {
    if (op === "+" && (a.t === "String" || b.t === "String")) return jstr(display(a) + display(b));
    if (op === "==" || op === "!=") return this.equality(op, a, b);
    if (isNumeric(a) && isNumeric(b)) {
      const dbl = a.t === "double" || b.t === "double";
      const lng = !dbl && (a.t === "long" || b.t === "long");
      const mk = dbl ? jdouble : lng ? jlong : jint;
      const x = num(a), y = num(b);
      switch (op) {
        case "+": return mk(x + y);
        case "-": return mk(x - y);
        case "*": return mk(x * y);
        case "/":
          if (!dbl) { if (y === 0) throw new RuntimeError("Division durch 0 ist nicht erlaubt.", pos); return mk(Math.trunc(x / y)); }
          return jdouble(x / y);
        case "%":
          if (!dbl && y === 0) throw new RuntimeError("Rest (%) durch 0 ist nicht erlaubt.", pos);
          return mk(x % y);
        case "<": return jbool(x < y);
        case ">": return jbool(x > y);
        case "<=": return jbool(x <= y);
        case ">=": return jbool(x >= y);
        case "&": return mk(x & y);
        case "|": return mk(x | y);
        case "^": return mk(x ^ y);
        case "<<": return mk(x << y);
        case ">>": return mk(x >> y);
        case ">>>": return mk(x >>> y);
      }
    }
    if ((op === "&&" || op === "||") && a.t === "boolean" && b.t === "boolean") {
      return jbool(op === "&&" ? a.v && b.v : a.v || b.v);
    }
    throw new RuntimeError(`Die Rechenoperation '${op}' passt nicht zu diesen Datentypen.`, pos);
  }

  private equality(op: string, a: JValue, b: JValue): JValue {
    let eq: boolean;
    if (a.t === "null" || b.t === "null") eq = a.t === "null" && b.t === "null";
    else if (isNumeric(a) && isNumeric(b)) eq = num(a) === num(b);
    else if (a.t === "boolean" && b.t === "boolean") eq = a.v === b.v;
    else if (a.t === "String" && b.t === "String") eq = a.v === b.v; // teaching simplification
    else eq = a === b;
    return jbool(op === "==" ? eq : !eq);
  }

  private unary(op: string, v: JValue, pos: Pos): JValue {
    switch (op) {
      case "!": if (v.t !== "boolean") throw new RuntimeError("'!' geht nur mit boolean.", pos); return jbool(!v.v);
      case "-": return v.t === "double" ? jdouble(-num(v)) : jint(-num(v));
      case "+": return v;
      case "~": return jint(~num(v));
      default: throw new RuntimeError(`Unbekannter Operator '${op}'.`, pos);
    }
  }

  private async assign(target: Expr, op: string, valueExpr: Expr, env: Environment): Promise<JValue> {
    let value = await this.evalExpr(valueExpr, env);
    if (op !== "=") {
      const current = await this.evalExpr(target, env);
      value = this.binary(op.slice(0, -1), current, value, target.pos);
    }
    if (target.kind === "name") {
      if (!env.assign(target.name, value)) throw new RuntimeError(`Die Variable '${target.name}' ist nicht bekannt.`, target.pos);
      return value;
    }
    if (target.kind === "index") {
      const arr = await this.evalExpr(target.array, env);
      const i = num(await this.evalExpr(target.index, env));
      if (arr.t !== "array") throw new RuntimeError("Hier wird ein Array erwartet.", target.pos);
      if (i < 0 || i >= arr.v.length) throw new RuntimeError(`Array-Index ${i} ist außerhalb (Länge ${arr.v.length}).`, target.pos);
      arr.v[i] = arr.elem && arr.elem !== "Object" ? coerceTo(arr.elem, value) : value;
      return value;
    }
    throw new RuntimeError("Dieser Zuweisung fehlt ein gültiges Ziel (Variable/Array).", target.pos);
  }

  private async update(target: Expr, op: "++" | "--", prefix: boolean, env: Environment, pos: Pos): Promise<JValue> {
    const current = await this.evalExpr(target, env);
    const delta = op === "++" ? 1 : -1;
    const next = current.t === "double" ? jdouble(num(current) + delta) : current.t === "long" ? jlong(num(current) + delta) : current.t === "char" ? jchar(num(current) + delta) : jint(num(current) + delta);
    if (target.kind === "name") env.assign(target.name, next);
    else if (target.kind === "index") {
      const arr = await this.evalExpr(target.array, env);
      const i = num(await this.evalExpr(target.index, env));
      if (arr.t === "array") arr.v[i] = next;
    } else throw new RuntimeError("++/-- braucht eine Variable.", pos);
    return prefix ? next : current;
  }

  private async newArray(expr: { type: { base: string; dims: number }; sizes: Expr[] }, env: Environment): Promise<JValue> {
    const sizes: number[] = [];
    for (const s of expr.sizes) sizes.push(num(await this.evalExpr(s, env)));
    const build = (depth: number): JValue => {
      if (depth >= sizes.length) return defaultValue(expr.type.base, 0);
      const n = sizes[depth]!;
      const v: JValue[] = [];
      for (let k = 0; k < n; k++) v.push(depth + 1 < sizes.length ? build(depth + 1) : defaultValue(expr.type.base, 0));
      return { t: "array", elem: expr.type.base, v };
    };
    return build(0);
  }

  private async newObject(expr: { type: { base: string }; args: Expr[]; pos: Pos }, env: Environment): Promise<JValue> {
    if (expr.type.base === "Scanner") return { t: "object", cls: "Scanner", native: { buffer: [], io: this.io } as ScannerState };
    if (expr.type.base === "String") {
      const a = expr.args[0] ? await this.evalExpr(expr.args[0], env) : jstr("");
      return jstr(a.t === "array" ? a.v.map((c) => String.fromCodePoint(num(c))).join("") : display(a));
    }
    throw new RuntimeError(`'new ${expr.type.base}(...)' wird (noch) nicht unterstützt.`, expr.pos);
  }

  // ---- field access & calls ----------------------------------------------
  private async fieldAccess(expr: { object: Expr; name: string; pos: Pos }, env: Environment): Promise<JValue> {
    const segs = flatten(expr as unknown as Expr);
    if (segs && segs.length === 2 && STATIC_CLASSES.has(segs[0]!) && !env.has(segs[0]!)) {
      const sf = staticField(segs[0]!, segs[1]!);
      if (sf) return sf;
    }
    const obj = await this.evalExpr(expr.object, env);
    if (obj.t === "array" && expr.name === "length") return jint(obj.v.length);
    throw new RuntimeError(`'${expr.name}' gibt es hier nicht.`, expr.pos);
  }

  private async call(expr: { callee: Expr; args: Expr[]; pos: Pos }, env: Environment): Promise<JValue> {
    const callee = expr.callee;
    if (callee.kind === "name") return this.userCall(callee.name, expr.args, env, expr.pos);
    if (callee.kind === "field") {
      const segs = flatten(callee);
      if (segs && STATIC_CLASSES.has(segs[0]!) && !env.has(segs[0]!)) {
        return this.staticCall(segs, expr.args, env, expr.pos);
      }
      const recv = await this.evalExpr(callee.object, env);
      return this.instanceCall(recv, callee.name, expr.args, env, expr.pos);
    }
    throw new RuntimeError("Dieser Methodenaufruf wird nicht unterstützt.", expr.pos);
  }

  private async userCall(name: string, argExprs: Expr[], env: Environment, pos: Pos): Promise<JValue> {
    const m = this.methods.get(name);
    if (!m) throw new RuntimeError(`Die Methode '${name}' gibt es nicht. Tippfehler?`, pos);
    if (m.params.length !== argExprs.length) {
      throw new RuntimeError(`'${name}' erwartet ${m.params.length} Argument(e), bekommt aber ${argExprs.length}.`, pos);
    }
    const callEnv = new Environment(null);
    for (let k = 0; k < m.params.length; k++) {
      callEnv.define(m.params[k]!.name, coerceTo(m.params[k]!.type.base, await this.evalExpr(argExprs[k]!, env)));
    }
    try { await this.execBlock(m.body, callEnv); }
    catch (e) { if (e instanceof ReturnSignal) return e.value; throw e; }
    return NULL;
  }

  private async args(argExprs: Expr[], env: Environment): Promise<JValue[]> {
    const out: JValue[] = [];
    for (const a of argExprs) out.push(await this.evalExpr(a, env));
    return out;
  }

  private async staticCall(segs: string[], argExprs: Expr[], env: Environment, pos: Pos): Promise<JValue> {
    const path = segs.join(".");
    // System.out.print / println
    if (path === "System.out.println" || path === "System.out.print") {
      const text = argExprs.length === 0 ? "" : display(await this.evalExpr(argExprs[0]!, env));
      this.io.print(path.endsWith("println") ? text + "\n" : text);
      return NULL;
    }
    const cls = segs[0]!, fn = segs[segs.length - 1]!;
    const a = await this.args(argExprs, env);
    if (cls === "Math") return this.mathCall(fn, a, pos);
    if (cls === "Integer") {
      if (fn === "parseInt") return jint(parseIntStrict(str(a[0]), pos));
      if (fn === "toString" || fn === "valueOf") return fn === "valueOf" ? jint(num(a[0]!)) : jstr(String(Math.trunc(num(a[0]!))));
      if (fn === "max") return jint(Math.max(num(a[0]!), num(a[1]!)));
      if (fn === "min") return jint(Math.min(num(a[0]!), num(a[1]!)));
    }
    if (cls === "Double" && (fn === "parseDouble" || fn === "valueOf")) return jdouble(parseFloatStrict(str(a[0]), pos));
    if (cls === "Long" && fn === "parseLong") return jlong(parseIntStrict(str(a[0]), pos));
    if (cls === "Boolean" && fn === "parseBoolean") return jbool(str(a[0]).trim().toLowerCase() === "true");
    if (cls === "String" && fn === "valueOf") return jstr(display(a[0]!));
    if (cls === "Arrays") return this.arraysCall(fn, a, pos);
    throw new RuntimeError(`'${path}(...)' wird (noch) nicht unterstützt.`, pos);
  }

  private mathCall(fn: string, a: JValue[], pos: Pos): JValue {
    const x = a[0] ? num(a[0]) : 0, y = a[1] ? num(a[1]) : 0;
    switch (fn) {
      case "abs": return a[0]!.t === "double" ? jdouble(Math.abs(x)) : jint(Math.abs(x));
      case "max": return a[0]!.t === "double" || a[1]!.t === "double" ? jdouble(Math.max(x, y)) : jint(Math.max(x, y));
      case "min": return a[0]!.t === "double" || a[1]!.t === "double" ? jdouble(Math.min(x, y)) : jint(Math.min(x, y));
      case "sqrt": return jdouble(Math.sqrt(x));
      case "pow": return jdouble(Math.pow(x, y));
      case "random": return jdouble(Math.random());
      case "round": return jlong(Math.round(x));
      case "floor": return jdouble(Math.floor(x));
      case "ceil": return jdouble(Math.ceil(x));
      case "sin": return jdouble(Math.sin(x));
      case "cos": return jdouble(Math.cos(x));
      case "tan": return jdouble(Math.tan(x));
      case "log": return jdouble(Math.log(x));
      case "log10": return jdouble(Math.log10(x));
      case "exp": return jdouble(Math.exp(x));
      case "cbrt": return jdouble(Math.cbrt(x));
      case "signum": return jdouble(Math.sign(x));
      case "toRadians": return jdouble(x * Math.PI / 180);
      case "toDegrees": return jdouble(x * 180 / Math.PI);
      default: throw new RuntimeError(`Math.${fn}(...) ist nicht bekannt.`, pos);
    }
  }

  private arraysCall(fn: string, a: JValue[], pos: Pos): JValue {
    const arr = a[0];
    if (!arr || arr.t !== "array") throw new RuntimeError(`Arrays.${fn} braucht ein Array.`, pos);
    if (fn === "toString") return jstr("[" + arr.v.map(display).join(", ") + "]");
    if (fn === "sort") { arr.v.sort((p, q) => num(p) - num(q)); return NULL; }
    if (fn === "fill") { for (let k = 0; k < arr.v.length; k++) arr.v[k] = a[1]!; return NULL; }
    throw new RuntimeError(`Arrays.${fn}(...) wird nicht unterstützt.`, pos);
  }

  private async instanceCall(recv: JValue, name: string, argExprs: Expr[], env: Environment, pos: Pos): Promise<JValue> {
    const a = await this.args(argExprs, env);
    if (recv.t === "String") return this.stringMethod(recv.v, name, a, pos);
    if (recv.t === "object" && recv.cls === "Scanner") return this.scannerMethod(recv.native as ScannerState, name, pos);
    if (recv.t === "array" && name === "length") return jint(recv.v.length);
    throw new RuntimeError(`Die Methode '${name}' gibt es für diesen Datentyp nicht.`, pos);
  }

  private stringMethod(s: string, name: string, a: JValue[], pos: Pos): JValue {
    switch (name) {
      case "length": return jint(s.length);
      case "charAt": { const i = num(a[0]!); if (i < 0 || i >= s.length) throw new RuntimeError(`charAt(${i}) ist außerhalb (Länge ${s.length}).`, pos); return jchar(s.codePointAt(i)!); }
      case "substring": return jstr(a[1] ? s.substring(num(a[0]!), num(a[1]!)) : s.substring(num(a[0]!)));
      case "indexOf": return jint(s.indexOf(display(a[0]!)));
      case "lastIndexOf": return jint(s.lastIndexOf(display(a[0]!)));
      case "contains": return jbool(s.includes(str(a[0])));
      case "equals": return jbool(a[0]!.t === "String" && a[0]!.v === s);
      case "equalsIgnoreCase": return jbool(a[0]!.t === "String" && a[0]!.v.toLowerCase() === s.toLowerCase());
      case "compareTo": return jint(s < str(a[0]) ? -1 : s > str(a[0]) ? 1 : 0);
      case "toUpperCase": return jstr(s.toUpperCase());
      case "toLowerCase": return jstr(s.toLowerCase());
      case "trim": case "strip": return jstr(s.trim());
      case "replace": return jstr(s.split(display(a[0]!)).join(display(a[1]!)));
      case "startsWith": return jbool(s.startsWith(str(a[0])));
      case "endsWith": return jbool(s.endsWith(str(a[0])));
      case "isEmpty": return jbool(s.length === 0);
      case "isBlank": return jbool(s.trim().length === 0);
      case "concat": return jstr(s + str(a[0]));
      case "repeat": return jstr(s.repeat(Math.max(0, num(a[0]!))));
      case "split": return { t: "array", elem: "String", v: s.split(new RegExp(str(a[0]))).map(jstr) };
      case "toCharArray": return { t: "array", elem: "char", v: Array.from(s).map((c) => jchar(c.codePointAt(0)!)) };
      case "charAtSafe": return jchar(s.codePointAt(0) ?? 0);
      default: throw new RuntimeError(`Die String-Methode '${name}' gibt es nicht.`, pos);
    }
  }

  private async scannerMethod(st: ScannerState, name: string, pos: Pos): Promise<JValue> {
    switch (name) {
      case "nextLine": { const line = await st.io.readLine(); if (line === null) throw new RuntimeError("Keine Eingabe mehr vorhanden (Scanner).", pos); return jstr(line); }
      case "next": return jstr(await nextToken(st, pos));
      case "nextInt": return jint(parseIntStrict(await nextToken(st, pos), pos));
      case "nextLong": return jlong(parseIntStrict(await nextToken(st, pos), pos));
      case "nextDouble": return jdouble(parseFloatStrict(await nextToken(st, pos), pos));
      case "nextBoolean": return jbool((await nextToken(st, pos)).toLowerCase() === "true");
      case "hasNext": case "hasNextLine": case "hasNextInt": return jbool(true);
      case "close": return NULL;
      default: throw new RuntimeError(`Die Scanner-Methode '${name}' gibt es nicht.`, pos);
    }
  }

  // ---- cooperative scheduling: keeps the UI alive + lets Stop interrupt ----
  private async checkpoint(pos: Pos): Promise<void> {
    if (this.ctrl.stopped) throw new StopSignal();
    if (++this.steps % 1500 === 0) {
      await new Promise((r) => setTimeout(r, 0));
      if (this.ctrl.stopped) throw new StopSignal();
    }
    if (this.steps > this.maxSteps) throw new RuntimeError("Das Programm läuft sehr lange – vermutlich eine Endlosschleife. Abgebrochen.", pos);
  }
}

// ---- helpers --------------------------------------------------------------
function flatten(e: Expr): string[] | null {
  if (e.kind === "name") return [e.name];
  if (e.kind === "field") { const base = flatten(e.object); return base ? [...base, e.name] : null; }
  return null;
}

function staticField(cls: string, field: string): JValue | null {
  if (cls === "Math") { if (field === "PI") return jdouble(Math.PI); if (field === "E") return jdouble(Math.E); }
  if (cls === "Integer") { if (field === "MAX_VALUE") return jint(2147483647); if (field === "MIN_VALUE") return jint(-2147483648); }
  if (cls === "Double") { if (field === "MAX_VALUE") return jdouble(Number.MAX_VALUE); if (field === "MIN_VALUE") return jdouble(Number.MIN_VALUE); }
  return null;
}

function str(v: JValue | undefined): string { return v ? display(v) : ""; }

function parseIntStrict(s: string, pos: Pos): number {
  const t = s.trim();
  if (!/^[+-]?\d+$/.test(t)) throw new RuntimeError(`'${s}' ist keine ganze Zahl.`, pos);
  return parseInt(t, 10);
}
function parseFloatStrict(s: string, pos: Pos): number {
  const t = s.trim();
  if (!/^[+-]?(\d+\.?\d*|\.\d+)([eE][+-]?\d+)?$/.test(t)) throw new RuntimeError(`'${s}' ist keine Zahl.`, pos);
  return parseFloat(t);
}

async function nextToken(st: ScannerState, pos: Pos): Promise<string> {
  while (st.buffer.length === 0) {
    const line = await st.io.readLine();
    if (line === null) throw new RuntimeError("Keine Eingabe mehr vorhanden (Scanner).", pos);
    for (const tok of line.split(/\s+/)) if (tok.length > 0) st.buffer.push(tok);
  }
  return st.buffer.shift()!;
}
