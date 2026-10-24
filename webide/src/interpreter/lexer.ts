import type { Pos } from "./ast";
import { ParseError } from "./errors";

export type TokKind = "num" | "str" | "char" | "name" | "keyword" | "punct" | "eof";

export interface Token {
  kind: TokKind;
  value: string;
  pos: Pos;
  numType?: "int" | "long" | "double"; // for num tokens
}

export const KEYWORDS = new Set([
  "if", "else", "while", "do", "for", "return", "break", "continue",
  "new", "true", "false", "null", "void", "int", "double", "boolean",
  "char", "long", "float", "short", "byte", "public", "private",
  "protected", "static", "final", "class", "this", "instanceof",
  "import", "package", "abstract",
]);

// Multi-char operators, longest first.
const OPERATORS = [
  ">>>=", "<<=", ">>=", ">>>", "...", "->", "::",
  "==", "!=", "<=", ">=", "&&", "||", "++", "--",
  "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<", ">>",
  "+", "-", "*", "/", "%", "=", "<", ">", "!", "~", "&", "|", "^",
  "(", ")", "{", "}", "[", "]", ";", ",", ".", "?", ":",
];

export function tokenize(src: string): Token[] {
  const tokens: Token[] = [];
  let i = 0;
  let line = 1;
  let col = 1;

  const here = (): Pos => ({ line, col });
  const advance = (n = 1): void => {
    for (let k = 0; k < n; k++) {
      if (src[i] === "\n") { line++; col = 1; } else { col++; }
      i++;
    }
  };

  while (i < src.length) {
    const c = src[i];

    // whitespace
    if (c === " " || c === "\t" || c === "\r" || c === "\n") { advance(); continue; }

    // comments
    if (c === "/" && src[i + 1] === "/") {
      while (i < src.length && src[i] !== "\n") advance();
      continue;
    }
    if (c === "/" && src[i + 1] === "*") {
      const start = here();
      advance(2);
      while (i < src.length && !(src[i] === "*" && src[i + 1] === "/")) advance();
      if (i >= src.length) throw new ParseError("Ein Kommentar /* … */ wurde nicht geschlossen.", start);
      advance(2);
      continue;
    }

    // string literal
    if (c === '"') {
      const start = here();
      advance();
      let value = "";
      while (i < src.length && src[i] !== '"') {
        if (src[i] === "\n") throw new ParseError('Ein Text (String) wurde nicht mit " geschlossen.', start);
        if (src[i] === "\\") { value += readEscape(); } else { value += src[i]; advance(); }
      }
      if (i >= src.length) throw new ParseError('Ein Text (String) wurde nicht mit " geschlossen.', start);
      advance(); // closing "
      tokens.push({ kind: "str", value, pos: start });
      continue;
    }

    // char literal
    if (c === "'") {
      const start = here();
      advance();
      let value = "";
      if (src[i] === "\\") value = readEscape();
      else if (src[i] === "'") throw new ParseError("Ein Zeichen '' ist leer.", start);
      else { value = src[i]!; advance(); }
      if (src[i] !== "'") throw new ParseError("Ein Zeichen wurde nicht mit ' geschlossen.", start);
      advance();
      tokens.push({ kind: "char", value, pos: start });
      continue;
    }

    // number
    if (isDigit(c!) || (c === "." && isDigit(src[i + 1] ?? ""))) {
      const start = here();
      let text = "";
      let isDouble = false;
      while (i < src.length && isDigit(src[i]!)) { text += src[i]; advance(); }
      if (src[i] === ".") { isDouble = true; text += "."; advance(); while (i < src.length && isDigit(src[i]!)) { text += src[i]; advance(); } }
      if (src[i] === "e" || src[i] === "E") {
        isDouble = true; text += src[i]; advance();
        if (src[i] === "+" || src[i] === "-") { text += src[i]; advance(); }
        while (i < src.length && isDigit(src[i]!)) { text += src[i]; advance(); }
      }
      let numType: "int" | "long" | "double" = isDouble ? "double" : "int";
      if (src[i] === "d" || src[i] === "D" || src[i] === "f" || src[i] === "F") { numType = "double"; advance(); }
      else if (src[i] === "l" || src[i] === "L") { numType = "long"; advance(); }
      tokens.push({ kind: "num", value: text, pos: start, numType });
      continue;
    }

    // identifier / keyword
    if (isIdentStart(c!)) {
      const start = here();
      let text = "";
      while (i < src.length && isIdentPart(src[i]!)) { text += src[i]; advance(); }
      tokens.push({ kind: KEYWORDS.has(text) ? "keyword" : "name", value: text, pos: start });
      continue;
    }

    // operator / punctuation
    const op = OPERATORS.find((o) => src.startsWith(o, i));
    if (op) {
      const start = here();
      advance(op.length);
      tokens.push({ kind: "punct", value: op, pos: start });
      continue;
    }

    throw new ParseError(`Unerwartetes Zeichen '${c}'.`, here());
  }

  tokens.push({ kind: "eof", value: "", pos: here() });
  return tokens;

  function readEscape(): string {
    advance(); // backslash
    const e = src[i];
    advance();
    switch (e) {
      case "n": return "\n";
      case "t": return "\t";
      case "r": return "\r";
      case "b": return "\b";
      case "f": return "\f";
      case "0": return "\0";
      case "\\": return "\\";
      case "'": return "'";
      case '"': return '"';
      default: return e ?? "";
    }
  }
}

function isDigit(c: string): boolean { return c >= "0" && c <= "9"; }
function isIdentStart(c: string): boolean { return /[A-Za-z_$]/.test(c); }
function isIdentPart(c: string): boolean { return /[A-Za-z0-9_$]/.test(c); }
