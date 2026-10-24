import "./style.css";
import { Decoration, EditorView, keymap, type DecorationSet } from "@codemirror/view";
import { StateEffect, StateField } from "@codemirror/state";
import { basicSetup } from "codemirror";
import { java } from "@codemirror/lang-java";
import { oneDark } from "@codemirror/theme-one-dark";
import { indentWithTab } from "@codemirror/commands";
import { autocompletion } from "@codemirror/autocomplete";
import { linter, type Diagnostic } from "@codemirror/lint";
import { runJava, type Controller, type DebugHook, type IO } from "./interpreter/run";
import { parseProgram } from "./interpreter/parser";
import { ParseError } from "./interpreter/errors";
import { javaCompletions } from "./completion";
import { loadProject, saveProject } from "./storage";
import { showHelp } from "./help";
import { showImports } from "./imports";

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
    <button class="tool-btn" id="btn-debug"><span class="ic">🐞</span>Debug</button>
    <button class="tool-btn" id="btn-step" style="display:none"><span class="ic">▶</span>Weiter</button>
    <button class="tool-btn" id="btn-stepend" style="display:none"><span class="ic">▶▶</span>Bis Ende</button>
    <span class="tool-sep"></span>
    <button class="tool-btn" id="btn-zoom-out"><span class="ic">🔍−</span></button>
    <button class="tool-btn" id="btn-zoom-in"><span class="ic">🔍+</span></button>
    <span class="spacer"></span>
    <button class="tool-btn" id="btn-help"><span class="ic">❔</span>Hilfe</button>
  </div>
  <div class="tabs">
    <div class="tab active">Programm.java</div>
  </div>
  <div class="editor-wrap" id="editor">
    <div class="debug-panel" id="debug-panel">
      <div class="debug-panel-title">Variablen</div>
      <table id="debug-vars"></table>
    </div>
  </div>
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

// ---- debugger: current-line highlight -----------------------------------
const setDebugLine = StateEffect.define<number | null>();
const debugLineField = StateField.define<DecorationSet>({
  create: () => Decoration.none,
  update(deco, tr) {
    deco = deco.map(tr.changes);
    for (const e of tr.effects) {
      if (e.is(setDebugLine)) {
        if (e.value == null) { deco = Decoration.none; }
        else {
          const n = Math.min(Math.max(1, e.value), tr.state.doc.lines);
          const line = tr.state.doc.line(n);
          deco = Decoration.set([Decoration.line({ class: "cm-debug-line" }).range(line.from)]);
        }
      }
    }
    return deco;
  },
  provide: (f) => EditorView.decorations.from(f),
});

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
    debugLineField,
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
let debugResolve: (() => void) | null = null;
let debugRunToEnd = false;

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
  debugRunToEnd = true;
  debugResolve?.();
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

// ---- debugger ------------------------------------------------------------
const btnDebug = document.querySelector<HTMLButtonElement>("#btn-debug")!;
const btnStep = document.querySelector<HTMLButtonElement>("#btn-step")!;
const btnStepEnd = document.querySelector<HTMLButtonElement>("#btn-stepend")!;
const debugPanel = document.querySelector<HTMLDivElement>("#debug-panel")!;
const debugVars = document.querySelector<HTMLTableElement>("#debug-vars")!;

const debugHook: DebugHook = {
  onStep: (line, vars) =>
    new Promise<void>((resolve) => {
      if (debugRunToEnd || ctrl.stopped) { resolve(); return; }
      editor.dispatch({ effects: setDebugLine.of(line) });
      renderVars(vars);
      debugResolve = () => { debugResolve = null; resolve(); };
    }),
};

function escapeHtml(s: string): string {
  return s.replace(/[&<>]/g, (c) => (c === "&" ? "&amp;" : c === "<" ? "&lt;" : "&gt;"));
}
function renderVars(vars: { name: string; value: string }[]): void {
  debugVars.innerHTML = vars.length
    ? vars.map((v) => `<tr><td class="dv-name">${escapeHtml(v.name)}</td><td class="dv-val">${escapeHtml(v.value)}</td></tr>`).join("")
    : `<tr><td class="dv-empty">(noch keine Variablen)</td></tr>`;
}
function setDebugUI(on: boolean): void {
  btnStep.style.display = on ? "" : "none";
  btnStepEnd.style.display = on ? "" : "none";
  btnDebug.style.display = on ? "none" : "";
  debugPanel.classList.toggle("active", on);
}

async function debugProgram(): Promise<void> {
  if (running) return;
  saveNow();
  clearConsole();
  inputQueue = [];
  inputWaiters = [];
  ctrl.stopped = false;
  debugRunToEnd = false;
  setRunning(true);
  setDebugUI(true);
  renderVars([]);
  try {
    const outcome = await runJava(
      editor.state.doc.toString(), io, ctrl,
      () => append('▶ Debugger gestartet – mit „Weiter" Schritt für Schritt.\n', "system"),
      debugHook,
    );
    switch (outcome.status) {
      case "ok": append("\n✓ Programm beendet\n", "system"); break;
      case "stopped": append("\n■ Programm gestoppt\n", "system"); break;
      case "compileError": append(`Fehler in Zeile ${outcome.line}: ${outcome.message}\n`, "error"); break;
      case "runtimeError": append((outcome.line !== null ? `Fehler in Zeile ${outcome.line}: ` : "Fehler: ") + outcome.message + "\n", "error"); break;
    }
  } finally {
    setRunning(false);
    setDebugUI(false);
    editor.dispatch({ effects: setDebugLine.of(null) });
    debugResolve = null;
    inputWaiters.forEach((w) => w(null));
    inputWaiters = [];
  }
}
btnDebug.addEventListener("click", () => void debugProgram());
btnStep.addEventListener("click", () => debugResolve?.());
btnStepEnd.addEventListener("click", () => { debugRunToEnd = true; editor.dispatch({ effects: setDebugLine.of(null) }); debugResolve?.(); });

// ---- toolbar buttons -----------------------------------------------------
document.querySelector<HTMLButtonElement>("#btn-save")!.addEventListener("click", saveNow);
document.querySelector<HTMLButtonElement>("#btn-help")!.addEventListener("click", showHelp);
document.querySelector<HTMLButtonElement>("#btn-imports")!.addEventListener("click", () => showImports(editor));

setSaved();

export {};
