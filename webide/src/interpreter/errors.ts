import type { Pos } from "./ast";

/** A compile-time error (lexing or parsing) with a beginner-friendly German message. */
export class ParseError extends Error {
  constructor(message: string, public pos: Pos) {
    super(message);
    this.name = "ParseError";
  }
}

/** A runtime error raised while interpreting. */
export class RuntimeError extends Error {
  constructor(message: string, public pos: Pos | null = null) {
    super(message);
    this.name = "RuntimeError";
  }
}

/** Signals that execution was stopped by the user (Stop button). */
export class StopSignal extends Error {
  constructor() {
    super("stopped");
    this.name = "StopSignal";
  }
}
