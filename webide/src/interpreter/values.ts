// Runtime values. Numeric type is tracked so Java semantics hold
// (e.g. 5 / 2 == 2, but 5.0 / 2 == 2.5).

export type JValue =
  | { t: "int"; v: number }
  | { t: "long"; v: number }
  | { t: "double"; v: number }
  | { t: "char"; v: number } // code point
  | { t: "boolean"; v: boolean }
  | { t: "String"; v: string }
  | { t: "null" }
  | { t: "array"; elem: string; v: JValue[] }
  | { t: "object"; cls: string; native: unknown };

export const NULL: JValue = { t: "null" };
export const jint = (v: number): JValue => ({ t: "int", v: Math.trunc(v) | 0 });
export const jlong = (v: number): JValue => ({ t: "long", v: Math.trunc(v) });
export const jdouble = (v: number): JValue => ({ t: "double", v });
export const jchar = (v: number): JValue => ({ t: "char", v });
export const jbool = (v: boolean): JValue => ({ t: "boolean", v });
export const jstr = (v: string): JValue => ({ t: "String", v });

export function isNumeric(x: JValue): boolean {
  return x.t === "int" || x.t === "long" || x.t === "double" || x.t === "char";
}

export function num(x: JValue): number {
  if (x.t === "int" || x.t === "long" || x.t === "double" || x.t === "char") return x.v;
  throw new Error("not numeric");
}

export function isInteger(x: JValue): boolean {
  return x.t === "int" || x.t === "long" || x.t === "char";
}

export function asBoolean(x: JValue): boolean {
  if (x.t === "boolean") return x.v;
  throw new Error("not boolean");
}

/** Java-like textual form used by println / string concatenation. */
export function display(x: JValue): string {
  switch (x.t) {
    case "int": case "long": return String(x.v);
    case "char": return String.fromCodePoint(x.v);
    case "boolean": return x.v ? "true" : "false";
    case "String": return x.v;
    case "null": return "null";
    case "double": return displayDouble(x.v);
    case "array": return "[" + x.v.map(display).join(", ") + "]";
    case "object": return x.cls + "@object";
  }
}

function displayDouble(v: number): string {
  if (Number.isNaN(v)) return "NaN";
  if (v === Infinity) return "Infinity";
  if (v === -Infinity) return "-Infinity";
  if (Number.isInteger(v)) return (Object.is(v, -0) ? "-0" : String(v)) + ".0";
  return String(v);
}

export function defaultValue(base: string, dims: number): JValue {
  if (dims > 0) return NULL;
  switch (base) {
    case "int": case "short": case "byte": return jint(0);
    case "long": return jlong(0);
    case "double": case "float": return jdouble(0);
    case "char": return jchar(0);
    case "boolean": return jbool(false);
    default: return NULL;
  }
}

/** Coerce a value to a declared variable/parameter type (widening only). */
export function coerceTo(base: string, x: JValue): JValue {
  if (x.t === "null") return x;
  switch (base) {
    case "int": case "short": case "byte": return jint(num(x));
    case "long": return jlong(num(x));
    case "double": case "float": return isNumeric(x) ? jdouble(num(x)) : x;
    case "char": return x.t === "char" ? x : jchar(num(x));
    case "boolean": case "String": case "var": default: return x;
  }
}
