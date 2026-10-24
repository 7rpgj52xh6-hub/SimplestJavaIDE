import type {
  Block, Expr, JType, MethodDecl, Param, Pos, Stmt, VarDeclarator,
} from "./ast";
import { ParseError } from "./errors";
import { KEYWORDS, tokenize, type Token } from "./lexer";

const PRIMITIVES = new Set(["int", "double", "boolean", "char", "long", "float", "short", "byte", "void"]);
const MODIFIERS = new Set(["public", "private", "protected", "static", "final", "abstract"]);
const ASSIGN_OPS = new Set(["=", "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", ">>>="]);

export function parseProgram(source: string): MethodDecl[] {
  return new Parser(tokenize(source)).parseProgram();
}

class Parser {
  private i = 0;
  constructor(private toks: Token[]) {}

  // ---- token helpers ------------------------------------------------------
  private peek(o = 0): Token { return this.toks[Math.min(this.i + o, this.toks.length - 1)]!; }
  private atEnd(): boolean { return this.peek().kind === "eof"; }
  private next(): Token { return this.toks[this.i++]!; }
  private is(v: string): boolean { const t = this.peek(); return (t.kind === "punct" || t.kind === "keyword") && t.value === v; }
  private match(v: string): boolean { if (this.is(v)) { this.i++; return true; } return false; }
  private expect(v: string, msg?: string): Token {
    if (this.is(v)) return this.next();
    throw new ParseError(msg ?? `Hier wird '${v}' erwartet.`, this.peek().pos);
  }
  private pos(): Pos { return this.peek().pos; }

  // ---- program & methods --------------------------------------------------
  parseProgram(): MethodDecl[] {
    const methods: MethodDecl[] = [];
    while (!this.atEnd()) {
      if (this.match(";")) continue;
      methods.push(this.parseMethod());
    }
    if (methods.length === 0) {
      throw new ParseError("Hier fehlt eine Methode, z. B. public static void main(String[] args){ … }.", this.pos());
    }
    return methods;
  }

  private parseMethod(): MethodDecl {
    const pos = this.pos();
    let isStatic = false;
    while (this.peek().kind === "keyword" && MODIFIERS.has(this.peek().value)) {
      if (this.next().value === "static") isStatic = true;
    }
    const returnType = this.parseType();
    const name = this.expectName("Hier wird ein Methodenname erwartet.").value;
    this.expect("(", "Nach dem Methodennamen wird '(' erwartet.");
    const params: Param[] = [];
    if (!this.is(")")) {
      do {
        while (this.peek().kind === "keyword" && this.peek().value === "final") this.next();
        const t = this.parseType();
        const pn = this.expectName("Hier wird ein Parametername erwartet.").value;
        params.push({ type: t, name: pn });
      } while (this.match(","));
    }
    this.expect(")", "Hier wird ')' erwartet.");
    const body = this.parseBlock();
    return { name, returnType, params, isStatic, body, pos };
  }

  private expectName(msg: string): Token {
    if (this.peek().kind === "name") return this.next();
    throw new ParseError(msg, this.pos());
  }

  // ---- types --------------------------------------------------------------
  private parseType(): JType {
    const t = this.peek();
    let base: string;
    if (t.kind === "keyword" && PRIMITIVES.has(t.value)) { base = this.next().value; }
    else if (t.kind === "name") { base = this.next().value; } // String, Scanner, var, user types …
    else throw new ParseError("Hier wird ein Datentyp erwartet (z. B. int, double, String).", t.pos);
    // skip generic arguments like <String, Integer>
    if (this.is("<")) this.skipGenerics();
    let dims = 0;
    while (this.is("[")) { this.next(); this.expect("]", "Hier wird ']' erwartet."); dims++; }
    return { base, dims };
  }

  private skipGenerics(): void {
    let depth = 0;
    do {
      const t = this.next();
      if (t.value === "<") depth++;
      else if (t.value === ">") depth--;
      else if (t.value === ">>") depth -= 2;
      if (this.atEnd()) throw new ParseError("Ein '<' bei den Typ-Angaben wurde nicht geschlossen.", t.pos);
    } while (depth > 0);
  }

  // ---- statements ---------------------------------------------------------
  private parseBlock(): Block {
    const pos = this.pos();
    this.expect("{", "Hier wird '{' erwartet.");
    const stmts: Stmt[] = [];
    while (!this.is("}") && !this.atEnd()) stmts.push(this.parseStatement());
    this.expect("}", "Eine '}' fehlt — die geschweifte Klammer wurde nicht geschlossen.");
    return { kind: "block", stmts, pos };
  }

  private parseStatement(): Stmt {
    const pos = this.pos();
    if (this.is("{")) return this.parseBlock();
    if (this.match(";")) return { kind: "empty", pos };
    if (this.is("if")) return this.parseIf();
    if (this.is("while")) return this.parseWhile();
    if (this.is("do")) return this.parseDoWhile();
    if (this.is("for")) return this.parseFor();
    if (this.match("return")) {
      const value = this.is(";") ? null : this.parseExpression();
      this.expect(";", "Hinter return fehlt ein ';'.");
      return { kind: "return", value, pos };
    }
    if (this.match("break")) { this.expect(";", "Hinter break fehlt ein ';'."); return { kind: "break", pos }; }
    if (this.match("continue")) { this.expect(";", "Hinter continue fehlt ein ';'."); return { kind: "continue", pos }; }

    if (this.looksLikeVarDecl()) return this.parseVarDecl();

    const expr = this.parseExpression();
    this.expect(";", "Hinter der Anweisung fehlt ein ';'.");
    return { kind: "exprstmt", expr, pos };
  }

  private parseIf(): Stmt {
    const pos = this.pos(); this.next();
    this.expect("(", "Nach if wird '(' erwartet.");
    const cond = this.parseExpression();
    this.expect(")", "Die Klammer nach der if-Bedingung fehlt ')'.");
    const then = this.parseStatement();
    const otherwise = this.match("else") ? this.parseStatement() : null;
    return { kind: "if", cond, then, otherwise, pos };
  }

  private parseWhile(): Stmt {
    const pos = this.pos(); this.next();
    this.expect("(", "Nach while wird '(' erwartet.");
    const cond = this.parseExpression();
    this.expect(")", "Die Klammer nach der while-Bedingung fehlt ')'.");
    return { kind: "while", cond, body: this.parseStatement(), pos };
  }

  private parseDoWhile(): Stmt {
    const pos = this.pos(); this.next();
    const body = this.parseStatement();
    this.expect("while", "Nach dem do-Block wird 'while' erwartet.");
    this.expect("(", "Nach while wird '(' erwartet.");
    const cond = this.parseExpression();
    this.expect(")", "')' fehlt.");
    this.expect(";", "Hinter do-while fehlt ein ';'.");
    return { kind: "do", body, cond, pos };
  }

  private parseFor(): Stmt {
    const pos = this.pos(); this.next();
    this.expect("(", "Nach for wird '(' erwartet.");
    // for-each:  for (Type name : iterable)
    if (this.looksLikeForEach()) {
      const type = this.parseType();
      const name = this.expectName("Hier wird ein Schleifen-Variablenname erwartet.").value;
      this.expect(":", "Beim for-each wird ':' erwartet.");
      const iterable = this.parseExpression();
      this.expect(")", "')' fehlt.");
      return { kind: "foreach", type, name, iterable, body: this.parseStatement(), pos };
    }
    let init: Stmt | null = null;
    if (!this.is(";")) {
      init = this.looksLikeVarDecl() ? this.parseVarDecl() : (() => {
        const e = this.parseExpression(); this.expect(";", "Im for-Kopf fehlt ein ';'."); return { kind: "exprstmt", expr: e, pos } as Stmt;
      })();
    } else { this.next(); }
    const cond = this.is(";") ? null : this.parseExpression();
    this.expect(";", "Im for-Kopf fehlt ein ';'.");
    const update: Expr[] = [];
    if (!this.is(")")) { do { update.push(this.parseExpression()); } while (this.match(",")); }
    this.expect(")", "Die Klammer des for-Kopfs fehlt ')'.");
    return { kind: "for", init, cond, update, body: this.parseStatement(), pos };
  }

  private parseVarDecl(): Stmt {
    const pos = this.pos();
    const type = this.parseType();
    const decls: VarDeclarator[] = [];
    do {
      const dpos = this.pos();
      const name = this.expectName("Hier wird ein Variablenname erwartet.").value;
      let extraDims = 0;
      while (this.is("[")) { this.next(); this.expect("]"); extraDims++; }
      const init = this.match("=") ? this.parseVarInit() : null;
      decls.push({ name, init, pos: dpos });
      void extraDims;
    } while (this.match(","));
    this.expect(";", "Hinter der Variablen fehlt ein ';'.");
    return { kind: "var", type, decls, pos };
  }

  private parseVarInit(): Expr {
    if (this.is("{")) return this.parseArrayInit();
    return this.parseExpression();
  }

  private parseArrayInit(): Expr {
    const pos = this.pos();
    this.expect("{");
    const elements: Expr[] = [];
    if (!this.is("}")) { do { if (this.is("}")) break; elements.push(this.parseVarInit()); } while (this.match(",")); }
    this.expect("}", "Eine '}' bei der Array-Liste fehlt.");
    return { kind: "arrayinit", elements, pos };
  }

  // ---- var-decl / for-each detection (backtracking) -----------------------
  private looksLikeVarDecl(): boolean {
    const save = this.i;
    try {
      const t = this.peek();
      const startsType = (t.kind === "keyword" && PRIMITIVES.has(t.value)) ||
        t.kind === "name"; // String, Scanner, var, user types …
      if (!startsType) return false;
      this.parseType();
      const ok = this.peek().kind === "name" &&
        (this.isNext("=", 1) || this.isNext(";", 1) || this.isNext(",", 1) || this.isNext(":", 1) || this.isNext("[", 1));
      return ok;
    } catch { return false; } finally { this.i = save; }
  }

  private looksLikeForEach(): boolean {
    const save = this.i;
    try {
      const t = this.peek();
      if (!((t.kind === "keyword" && PRIMITIVES.has(t.value)) || t.kind === "name")) return false;
      this.parseType();
      if (this.peek().kind !== "name") return false;
      this.next();
      return this.is(":");
    } catch { return false; } finally { this.i = save; }
  }

  private isNext(v: string, o: number): boolean { const t = this.peek(o); return (t.kind === "punct" || t.kind === "keyword") && t.value === v; }

  // ---- expressions (precedence climbing) ----------------------------------
  private parseExpression(): Expr { return this.parseAssignment(); }

  private parseAssignment(): Expr {
    const left = this.parseTernary();
    const t = this.peek();
    if (t.kind === "punct" && ASSIGN_OPS.has(t.value)) {
      this.next();
      const value = this.parseAssignment();
      return { kind: "assign", target: left, op: t.value, value, pos: t.pos };
    }
    return left;
  }

  private parseTernary(): Expr {
    const cond = this.parseBinary(0);
    if (this.is("?")) {
      const pos = this.next().pos;
      const then = this.parseAssignment();
      this.expect(":", "Beim ?:-Ausdruck fehlt ':'.");
      const otherwise = this.parseAssignment();
      return { kind: "ternary", cond, then, otherwise, pos };
    }
    return cond;
  }

  // binary operator precedence table (higher binds tighter)
  private static readonly BIN: Record<string, number> = {
    "||": 1, "&&": 2, "|": 3, "^": 4, "&": 5,
    "==": 6, "!=": 6, "<": 7, ">": 7, "<=": 7, ">=": 7, "instanceof": 7,
    "<<": 8, ">>": 8, ">>>": 8, "+": 9, "-": 9, "*": 10, "/": 10, "%": 10,
  };

  private parseBinary(minPrec: number): Expr {
    let left = this.parseUnary();
    for (;;) {
      const t = this.peek();
      const op = t.value;
      const prec = (t.kind === "punct" || t.kind === "keyword") ? Parser.BIN[op] : undefined;
      if (prec === undefined || prec < minPrec) break;
      this.next();
      if (op === "instanceof") { this.parseType(); left = { kind: "lit", value: true, type: "boolean", pos: t.pos }; continue; }
      const right = this.parseBinary(prec + 1);
      if (op === "&&" || op === "||") left = { kind: "logical", op, left, right, pos: t.pos };
      else left = { kind: "bin", op, left, right, pos: t.pos };
    }
    return left;
  }

  private parseUnary(): Expr {
    const t = this.peek();
    if (t.kind === "punct" && (t.value === "!" || t.value === "-" || t.value === "+" || t.value === "~")) {
      this.next();
      return { kind: "unary", op: t.value, operand: this.parseUnary(), pos: t.pos };
    }
    if (t.kind === "punct" && (t.value === "++" || t.value === "--")) {
      this.next();
      return { kind: "update", op: t.value as "++" | "--", operand: this.parseUnary(), prefix: true, pos: t.pos };
    }
    return this.parsePostfix();
  }

  private parsePostfix(): Expr {
    let e = this.parsePrimary();
    for (;;) {
      const t = this.peek();
      if (t.kind === "punct" && t.value === ".") {
        this.next();
        const name = this.expectName("Nach '.' wird ein Name erwartet.").value;
        e = { kind: "field", object: e, name, pos: t.pos };
      } else if (t.kind === "punct" && t.value === "(") {
        const pos = this.next().pos;
        const args: Expr[] = [];
        if (!this.is(")")) { do { args.push(this.parseExpression()); } while (this.match(",")); }
        this.expect(")", "Beim Methodenaufruf fehlt ')'.");
        e = { kind: "call", callee: e, args, pos };
      } else if (t.kind === "punct" && t.value === "[") {
        const pos = this.next().pos;
        const index = this.parseExpression();
        this.expect("]", "Beim Array-Zugriff fehlt ']'.");
        e = { kind: "index", array: e, index, pos };
      } else if (t.kind === "punct" && (t.value === "++" || t.value === "--")) {
        this.next();
        e = { kind: "update", op: t.value as "++" | "--", operand: e, prefix: false, pos: t.pos };
      } else break;
    }
    return e;
  }

  private parsePrimary(): Expr {
    const t = this.peek();
    if (t.kind === "num") {
      this.next();
      const value = t.numType === "double" ? parseFloat(t.value) : parseInt(t.value, 10);
      return { kind: "lit", value, type: t.numType === "double" ? "double" : t.numType === "long" ? "long" : "int", pos: t.pos };
    }
    if (t.kind === "str") { this.next(); return { kind: "lit", value: t.value, type: "String", pos: t.pos }; }
    if (t.kind === "char") { this.next(); return { kind: "lit", value: t.value, type: "char", pos: t.pos }; }
    if (t.kind === "keyword" && t.value === "true") { this.next(); return { kind: "lit", value: true, type: "boolean", pos: t.pos }; }
    if (t.kind === "keyword" && t.value === "false") { this.next(); return { kind: "lit", value: false, type: "boolean", pos: t.pos }; }
    if (t.kind === "keyword" && t.value === "null") { this.next(); return { kind: "lit", value: null, type: "null", pos: t.pos }; }
    if (t.kind === "keyword" && t.value === "this") { this.next(); return { kind: "name", name: "this", pos: t.pos }; }
    if (t.kind === "keyword" && t.value === "new") return this.parseNew();
    if (t.kind === "name") { this.next(); return { kind: "name", name: t.value, pos: t.pos }; }
    if (this.match("(")) { const e = this.parseExpression(); this.expect(")", "Eine schließende ')' fehlt."); return e; }
    throw new ParseError("Hier fehlt oder steht ein unerwarteter Ausdruck.", t.pos);
  }

  private parseNew(): Expr {
    const pos = this.next().pos; // 'new'
    const type = this.parseTypeNameOnly();
    if (this.is("[")) {
      const sizes: Expr[] = [];
      while (this.is("[")) {
        this.next();
        if (this.is("]")) { this.next(); type.dims++; } else { sizes.push(this.parseExpression()); this.expect("]"); }
      }
      if (this.is("{")) { const init = this.parseArrayInit(); return init; }
      return { kind: "newarray", type, sizes, pos };
    }
    this.expect("(", "Nach 'new Typ' wird '(' erwartet.");
    const args: Expr[] = [];
    if (!this.is(")")) { do { args.push(this.parseExpression()); } while (this.match(",")); }
    this.expect(")", "')' fehlt.");
    return { kind: "new", type, args, pos };
  }

  private parseTypeNameOnly(): JType {
    const t = this.peek();
    let base: string;
    if (t.kind === "keyword" && PRIMITIVES.has(t.value)) base = this.next().value;
    else if (t.kind === "name") base = this.next().value;
    else throw new ParseError("Nach 'new' wird ein Typ erwartet.", t.pos);
    if (this.is("<")) this.skipGenerics();
    return { base, dims: 0 };
  }
}

void KEYWORDS;
