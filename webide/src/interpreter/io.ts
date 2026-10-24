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
