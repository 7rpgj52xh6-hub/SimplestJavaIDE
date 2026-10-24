// Abstract syntax tree for the teaching subset of Java.

export interface JType {
  base: string; // int, double, boolean, char, long, String, void, or a user type
  dims: number; // array dimensions (0 = not an array)
}

export interface Pos {
  line: number;
  col: number;
}

// ---- expressions ----------------------------------------------------------
export type Expr =
  | Literal
  | NameExpr
  | Binary
  | Logical
  | Unary
  | Update
  | Assign
  | Ternary
  | Call
  | FieldAccess
  | IndexAccess
  | NewArray
  | NewObject
  | ArrayInit;

export interface Literal { kind: "lit"; value: unknown; type: string; pos: Pos; }
export interface NameExpr { kind: "name"; name: string; pos: Pos; }
export interface Binary { kind: "bin"; op: string; left: Expr; right: Expr; pos: Pos; }
export interface Logical { kind: "logical"; op: "&&" | "||"; left: Expr; right: Expr; pos: Pos; }
export interface Unary { kind: "unary"; op: string; operand: Expr; pos: Pos; }
export interface Update { kind: "update"; op: "++" | "--"; operand: Expr; prefix: boolean; pos: Pos; }
export interface Assign { kind: "assign"; target: Expr; op: string; value: Expr; pos: Pos; }
export interface Ternary { kind: "ternary"; cond: Expr; then: Expr; otherwise: Expr; pos: Pos; }
export interface Call { kind: "call"; callee: Expr; args: Expr[]; pos: Pos; }
export interface FieldAccess { kind: "field"; object: Expr; name: string; pos: Pos; }
export interface IndexAccess { kind: "index"; array: Expr; index: Expr; pos: Pos; }
export interface NewArray { kind: "newarray"; type: JType; sizes: Expr[]; pos: Pos; }
export interface NewObject { kind: "new"; type: JType; args: Expr[]; pos: Pos; }
export interface ArrayInit { kind: "arrayinit"; elements: Expr[]; pos: Pos; }

// ---- statements -----------------------------------------------------------
export type Stmt =
  | VarDecl
  | ExprStmt
  | IfStmt
  | WhileStmt
  | DoWhileStmt
  | ForStmt
  | ForEachStmt
  | Block
  | BreakStmt
  | ContinueStmt
  | ReturnStmt
  | EmptyStmt;

export interface VarDeclarator { name: string; init: Expr | null; pos: Pos; }
export interface VarDecl { kind: "var"; type: JType; decls: VarDeclarator[]; pos: Pos; }
export interface ExprStmt { kind: "exprstmt"; expr: Expr; pos: Pos; }
export interface IfStmt { kind: "if"; cond: Expr; then: Stmt; otherwise: Stmt | null; pos: Pos; }
export interface WhileStmt { kind: "while"; cond: Expr; body: Stmt; pos: Pos; }
export interface DoWhileStmt { kind: "do"; body: Stmt; cond: Expr; pos: Pos; }
export interface ForStmt { kind: "for"; init: Stmt | null; cond: Expr | null; update: Expr[]; body: Stmt; pos: Pos; }
export interface ForEachStmt { kind: "foreach"; type: JType; name: string; iterable: Expr; body: Stmt; pos: Pos; }
export interface Block { kind: "block"; stmts: Stmt[]; pos: Pos; }
export interface BreakStmt { kind: "break"; pos: Pos; }
export interface ContinueStmt { kind: "continue"; pos: Pos; }
export interface ReturnStmt { kind: "return"; value: Expr | null; pos: Pos; }
export interface EmptyStmt { kind: "empty"; pos: Pos; }

// ---- methods --------------------------------------------------------------
export interface Param { type: JType; name: string; }
export interface MethodDecl {
  name: string;
  returnType: JType;
  params: Param[];
  isStatic: boolean;
  body: Block;
  pos: Pos;
}
