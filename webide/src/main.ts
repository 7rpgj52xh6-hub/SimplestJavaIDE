import "./style.css";
import { EditorView, keymap } from "@codemirror/view";
import { basicSetup } from "codemirror";
import { java } from "@codemirror/lang-java";
import { oneDark } from "@codemirror/theme-one-dark";
import { indentWithTab } from "@codemirror/commands";
import { autocompletion } from "@codemirror/autocomplete";
import { linter, type Diagnostic } from "@codemirror/lint";
import { runJava, type Controller, type IO } from "./interpreter/run";
import { parseProgram } from "./interpreter/parser";
import { ParseError } from "./interpreter/errors";
import { javaCompletions } from "./completion";
import { loadProject, saveProject } from "./storage";
import { showHelp } from "./help";

const DEFAULT_CODE = `public static void main(String[] args){
\tSystem.out.println("Hello World");
}`;

type OutKind = "output" | "error" | "system" | "hint" | "input";

const app = document.querySelector<HTMLDivElement>("#app")!;
app.innerHTML = `
  <div class="toolbar">
    <button class="tool-btn run" id="btn-run"><span class="ic">▶</span>Run</button>
    <button class="tool-btn stop" id="btn-stop" disabled><span class="ic">■</span>Stop</button>
    <span class="tool-sep"></span>
    <button class="tool-btn" id="btn-save"><span class="ic">💾</span>Speichern</button>
    <button class="tool-btn" id="btn-imports"><span class="ic">⬇</span>Imports</button>
    <span class="tool-sep"></span>
    <button class="tool-btn" id="btn-zoom-out"><span class="ic">🔍−</span></button>
    <button class="tool-btn" id="btn-zoom-in"><span class="ic">🔍+</span></button>
    <span class="spacer"></span>
    <button class="tool-btn" id="btn-help"><span class="ic">❔</span>Hilfe</button>
  </div>
  <div class="tabs">
    <div class="tab active" id="tab-main">Main Method</div>
    <div class="tab-add" id="tab-add" title="Neue Methode">+</div>
  </div>
  <div class="editor-wrap" id="editor"></div>
  <div class="console">
    <div class="console-bar">
      <label for="console-input">Input</label>
      <input class="console-input" id="console-input" type="text" autocomplete="off" placeholder="Eingabe für das Programm…" />
      <button class="tool-btn" id="btn-send">⏎ Senden</button>
      <button class="tool-btn" id="btn-clear">Clear</button>
    </div>
    <pre class="console-output" id="console-output"></pre>
  </div>
  <div class="statusbar" id="statusbar">✓ Gespeichert</div>
`;

// ---- live syntax-error linter -------------------------------------------
const javaLinter = linter((view): Diagnostic[] => {
  const src = view.state.doc.toString();
  try {
    parseProgram(src);
    return [];
  } catch (e) {
    if (!(e instanceof ParseError)) return [];
    const lineNo = Math.min(Math.max(1, e.pos.line), view.state.doc.lines);
    const line = view.state.doc.line(lineNo);
    const from = Math.min(line.from + Math.max(0, e.pos.col - 1), line.to);
    return [{ from, to: Math.min(from + 1, line.to) || line.to, severity: "error", message: e.message }];
  }
}, { delay: 400 });

// ---- editor --------------------------------------------------------------
let fontSize = 14;
const saved = loadProject();
const editor = new EditorView({
  doc: saved?.code ?? DEFAULT_CODE,
  extensions: [
    keymap.of([
      { key: "Mod-s", preventDefault: true, run: () => { saveNow(); return true; } },
      { key: "Mod-Enter", preventDefault: true, run: () => { void runProgram(); return true; } },
      { key: "F5", preventDefault: true, run: () => { void runProgram(); return true; } },
      indentWithTab,
    ]),
    basicSetup,
    java(),
    oneDark,
    autocompletion({ override: [javaCompletions] }),
    javaLinter,
    EditorView.updateListener.of((u) => { if (u.docChanged) onEdited(); }),
  ],
  parent: document.querySelector<HTMLDivElement>("#editor")!,
});

// ---- console -------------------------------------------------------------
const outputEl = document.querySelector<HTMLPreElement>("#console-output")!;
const inputEl = document.querySelector<HTMLInputElement>("#console-input")!;

function append(text: string, kind: OutKind = "output"): void {
  const span = document.createElement("span");
  span.className = "out-" + kind;
  span.textContent = text;
  outputEl.appendChild(span);
  outputEl.scrollTop = outputEl.scrollHeight;
}
function clearConsole(): void { outputEl.textContent = ""; }

// ---- status / save -------------------------------------------------------
const statusEl = document.querySelector<HTMLDivElement>("#statusbar")!;
let saveTimer: number | undefined;

function setSaved(): void { statusEl.textContent = "✓ Gespeichert"; statusEl.classList.remove("dirty"); }
function setDirty(): void { statusEl.textContent = "● Nicht gespeichert"; statusEl.classList.add("dirty"); }

function saveNow(): void {
  saveProject(editor.state.doc.toString());
  setSaved();
}
function onEdited(): void {
  setDirty();
  window.clearTimeout(saveTimer);
  saveTimer = window.setTimeout(saveNow, 1200); // autosave
}

// ---- stdin plumbing ------------------------------------------------------
let inputQueue: string[] = [];
let inputWaiters: Array<(line: string | null) => void> = [];

const io: IO = {
  print: (text) => append(text, "output"),
  readLine: () =>
    new Promise<string | null>((resolve) => {
      const queued = inputQueue.shift();
      if (queued !== undefined) resolve(queued);
      else inputWaiters.push(resolve);
    }),
};

function deliverInput(line: string): void {
  const waiter = inputWaiters.shift();
  if (waiter) waiter(line);
  else inputQueue.push(line);
}

// ---- run / stop ----------------------------------------------------------
const btnRun = document.querySelector<HTMLButtonElement>("#btn-run")!;
const btnStop = document.querySelector<HTMLButtonElement>("#btn-stop")!;
let running = false;
const ctrl: Controller = { stopped: false };

function setRunning(on: boolean): void {
  running = on;
  btnRun.disabled = on;
  btnStop.disabled = !on;
}

async function runProgram(): Promise<void> {
  if (running) return;
  saveNow();
  clearConsole();
  inputQueue = [];
  inputWaiters = [];
  ctrl.stopped = false;
  setRunning(true);
  inputEl.focus();
  try {
    const outcome = await runJava(editor.state.doc.toString(), io, ctrl, () => append("▶ Programm gestartet\n", "system"));
    switch (outcome.status) {
      case "ok": append("\n✓ Programm beendet\n", "system"); break;
      case "stopped": append("\n■ Programm gestoppt\n", "system"); break;
      case "compileError": append(`Fehler in Zeile ${outcome.line}: ${outcome.message}\n`, "error"); break;
      case "runtimeError": append((outcome.line !== null ? `Fehler in Zeile ${outcome.line}: ` : "Fehler: ") + outcome.message + "\n", "error"); break;
    }
  } finally {
    setRunning(false);
    inputWaiters.forEach((w) => w(null));
    inputWaiters = [];
  }
}

function stopProgram(): void {
  ctrl.stopped = true;
  inputWaiters.forEach((w) => w(null));
  inputWaiters = [];
}

btnRun.addEventListener("click", () => void runProgram());
btnStop.addEventListener("click", stopProgram);

// ---- input ---------------------------------------------------------------
function sendInput(): void {
  const text = inputEl.value;
  inputEl.value = "";
  if (!running) return;
  append("> " + text + "\n", "input");
  deliverInput(text);
}
document.querySelector<HTMLButtonElement>("#btn-send")!.addEventListener("click", sendInput);
inputEl.addEventListener("keydown", (e) => { if (e.key === "Enter") sendInput(); });
document.querySelector<HTMLButtonElement>("#btn-clear")!.addEventListener("click", clearConsole);

// ---- zoom ----------------------------------------------------------------
function applyZoom(delta: number): void {
  fontSize = Math.max(10, Math.min(28, fontSize + delta));
  editor.dom.style.fontSize = fontSize + "px";
}
document.querySelector<HTMLButtonElement>("#btn-zoom-in")!.addEventListener("click", () => applyZoom(1));
document.querySelector<HTMLButtonElement>("#btn-zoom-out")!.addEventListener("click", () => applyZoom(-1));

// ---- toolbar buttons -----------------------------------------------------
document.querySelector<HTMLButtonElement>("#btn-save")!.addEventListener("click", saveNow);
document.querySelector<HTMLButtonElement>("#btn-help")!.addEventListener("click", showHelp);
document.querySelector<HTMLButtonElement>("#btn-imports")!.addEventListener("click", () => append("(Imports-Dialog folgt)\n", "hint"));

setSaved();

export {};
