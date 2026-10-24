import { ParseError, RuntimeError, StopSignal } from "./errors";
import { Interpreter } from "./interpreter";
import type { Controller, IO } from "./io";
import { parseProgram } from "./parser";

export type RunOutcome =
  | { status: "ok" }
  | { status: "stopped" }
  | { status: "compileError"; message: string; line: number }
  | { status: "runtimeError"; message: string; line: number | null };

export type { Controller, IO };

/** Parse and run the given Java source; program output goes through `io`.
 *  `onStart` runs after a successful parse, just before execution begins. */
export async function runJava(source: string, io: IO, ctrl: Controller, onStart?: () => void): Promise<RunOutcome> {
  let methods;
  try {
    methods = parseProgram(source);
  } catch (e) {
    if (e instanceof ParseError) return { status: "compileError", message: e.message, line: e.pos.line };
    throw e;
  }

  onStart?.();
  const interpreter = new Interpreter(methods, io, ctrl);
  try {
    await interpreter.run();
    return ctrl.stopped ? { status: "stopped" } : { status: "ok" };
  } catch (e) {
    if (ctrl.stopped || e instanceof StopSignal) return { status: "stopped" };
    if (e instanceof RuntimeError) return { status: "runtimeError", message: e.message, line: e.pos?.line ?? null };
    return { status: "runtimeError", message: "Interner Fehler: " + (e instanceof Error ? e.message : String(e)), line: null };
  }
}
