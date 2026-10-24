/** How the interpreter talks to the console (output and stdin). */
export interface IO {
  /** Write program output (stdout). */
  print(text: string): void;
  /** Read one line of input; resolves when the user sends a line (null = end of input). */
  readLine(): Promise<string | null>;
}

/** Lets the UI stop a running program. */
export interface Controller {
  stopped: boolean;
}

/** One visible variable while stepping through the program. */
export interface DebugVar {
  name: string;
  value: string;
}

/** The debugger pauses before each statement; onStep resolves when the user steps on. */
export interface DebugHook {
  onStep(line: number, vars: DebugVar[]): Promise<void>;
}
