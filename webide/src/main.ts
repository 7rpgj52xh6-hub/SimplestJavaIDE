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
\t// Schreibe hier deinen Code und klicke oben auf Run ▶
\tSystem.out.println("Hallo Welt");
}`;

type OutKind = "output" | "error" | "system" | "hint" | "input";

const app = document.querySelector<HTMLDivElement>("#app")!;
app.innerHTML = `
  <div class="toolbar">
    <div class="brand"><span class="brand-badge">sJ</span><span class="brand-name">SimplestJavaIDE</span></div>
    <span class="tool-sep"></span>
    <button class="tool-btn run" id="btn-run" title="Ausführen (F5 / Strg+Enter)"><svg class="ic" viewBox="0 0 16 16"><path d="M4 3l9 5-9 5z" fill="currentColor"/></svg>Run</button>
    <button class="tool-btn stop" id="btn-stop" disabled title="Stoppen"><svg class="ic" viewBox="0 0 16 16"><rect x="4" y="4" width="8" height="8" rx="1.5" fill="currentColor"/></svg>Stop</button>
    <span class="tool-sep"></span>
    <button class="tool-btn" id="btn-save" title="Speichern (Strg/Cmd+S)"><svg class="ic" viewBox="0 0 16 16"><path d="M3 2.5h8l2.5 2.5v8.5H3z" fill="none" stroke="currentColor" stroke-width="1.3"/><rect x="5" y="2.5" width="4.5" height="3.3" fill="currentColor"/><rect x="5" y="8.5" width="6" height="4.5" fill="none" stroke="currentColor" stroke-width="1.1"/></svg>Speichern</button>
    <button class="tool-btn" id="btn-imports" title="Imports"><svg class="ic" viewBox="0 0 16 16"><path d="M8 2v7m0 0l-3-3m3 3l3-3M3 13h10" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>Imports</button>
    <button class="tool-btn" id="btn-debug" title="Debuggen (Schritt für Schritt)"><svg class="ic" viewBox="0 0 16 16"><ellipse cx="8" cy="9" rx="3.3" ry="4" fill="none" stroke="currentColor" stroke-width="1.3"/><path d="M8 5.2V3M5 6L3.2 4.2m7.8 1.8L12.8 4.2M4.9 9H2.3m11.4 0h-2.6M5.2 12l-1.8 1.8m9.4-1.8l-1.8 1.8" stroke="currentColor" stroke-width="1.1" stroke-linecap="round"/></svg>Debug</button>
    <button class="tool-btn" id="btn-step" style="display:none" title="Einen Schritt weiter"><svg class="ic" viewBox="0 0 16 16"><path d="M4 3l9 5-9 5z" fill="currentColor"/></svg>Weiter</button>
    <button class="tool-btn" id="btn-stepend" style="display:none" title="Bis zum Ende laufen"><svg class="ic" viewBox="0 0 16 16"><path d="M3 3l5 5-5 5zM9 3l5 5-5 5z" fill="currentColor"/></svg>Bis Ende</button>
    <span class="tool-sep"></span>
    <button class="tool-btn icon-only" id="btn-zoom-out" title="Schrift kleiner"><svg class="ic" viewBox="0 0 16 16"><circle cx="6.5" cy="6.5" r="4.3" fill="none" stroke="currentColor" stroke-width="1.4"/><path d="M9.7 9.7L14 14M4.6 6.5h3.8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg></button>
    <button class="tool-btn icon-only" id="btn-zoom-in" title="Schrift größer"><svg class="ic" viewBox="0 0 16 16"><circle cx="6.5" cy="6.5" r="4.3" fill="none" stroke="currentColor" stroke-width="1.4"/><path d="M9.7 9.7L14 14M4.6 6.5h3.8M6.5 4.6v3.8" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg></button>
    <span class="spacer"></span>
    <button class="tool-btn" id="btn-help" title="Hilfe"><svg class="ic" viewBox="0 0 16 16"><circle cx="8" cy="8" r="6.2" fill="none" stroke="currentColor" stroke-width="1.3"/><path d="M6.2 6.2c0-1 .8-1.8 1.9-1.8s1.8.7 1.8 1.7c0 1.4-1.7 1.4-1.7 2.8" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/><circle cx="8.1" cy="11.2" r=".85" fill="currentColor"/></svg>Hilfe</button>
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
