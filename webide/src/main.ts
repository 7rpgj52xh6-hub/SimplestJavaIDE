import "./style.css";
import { EditorView, keymap } from "@codemirror/view";
import { basicSetup } from "codemirror";
import { java } from "@codemirror/lang-java";
import { oneDark } from "@codemirror/theme-one-dark";
import { indentWithTab } from "@codemirror/commands";

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
      <input class="console-input" id="console-input" type="text" autocomplete="off" />
      <button class="tool-btn" id="btn-send">⏎ Senden</button>
      <button class="tool-btn" id="btn-clear">Clear</button>
    </div>
    <pre class="console-output" id="console-output"></pre>
  </div>
  <div class="statusbar" id="statusbar">✓ Gespeichert</div>
`;

// ---- editor --------------------------------------------------------------
let fontSize = 14;
const editor = new EditorView({
  doc: DEFAULT_CODE,
  extensions: [basicSetup, keymap.of([indentWithTab]), java(), oneDark],
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

function clearConsole(): void {
  outputEl.textContent = "";
}

// ---- run (interpreter is wired up in the next step) ----------------------
const btnRun = document.querySelector<HTMLButtonElement>("#btn-run")!;
const btnStop = document.querySelector<HTMLButtonElement>("#btn-stop")!;

function runProgram(): void {
  clearConsole();
  append("▶ Programm gestartet\n", "system");
  append("Der Java-Interpreter wird gerade gebaut – im nächsten Schritt läuft dein Code hier wirklich.\n", "hint");
  append("✓ Programm beendet\n", "system");
}

btnRun.addEventListener("click", runProgram);
btnStop.addEventListener("click", () => append("■ Gestoppt\n", "system"));

// ---- input ---------------------------------------------------------------
function sendInput(): void {
  const text = inputEl.value;
  if (text.length === 0) return;
  append("> " + text + "\n", "input");
  inputEl.value = "";
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

// ---- placeholders for not-yet-built features -----------------------------
document.querySelector<HTMLButtonElement>("#btn-save")!.addEventListener("click", () => {
  append("(Speichern folgt)\n", "hint");
});
document.querySelector<HTMLButtonElement>("#btn-imports")!.addEventListener("click", () => {
  append("(Imports-Dialog folgt)\n", "hint");
});
document.querySelector<HTMLButtonElement>("#btn-help")!.addEventListener("click", () => {
  append("(Hilfe folgt)\n", "hint");
});

export {};
