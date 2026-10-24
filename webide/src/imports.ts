import type { EditorView } from "@codemirror/view";

const QUICK: [string, string][] = [
  ["Scanner", "java.util.Scanner"],
  ["ArrayList", "java.util.ArrayList"],
  ["List", "java.util.List"],
  ["Random", "java.util.Random"],
  ["Arrays", "java.util.Arrays"],
  ["HashMap", "java.util.HashMap"],
];

/** A small dialog to insert `import` lines at the top of the program. */
export function showImports(editor: EditorView): void {
  const open = document.getElementById("imports-overlay");
  if (open) { open.remove(); return; }
  const overlay = document.createElement("div");
  overlay.id = "imports-overlay";
  overlay.className = "modal-overlay";
  overlay.innerHTML = `
    <div class="modal">
      <h2>Imports hinzufügen</h2>
      <p>Fügt oben eine <code>import</code>-Zeile ein. In dieser IDE sind Imports
         optional – die Namen funktionieren auch ohne –, aber so siehst du, wie es
         in echtem Java aussieht.</p>
      <div class="imports-quick">
        ${QUICK.map(([label, pkg]) => `<button class="tool-btn" data-pkg="${pkg}">${label}</button>`).join("")}
      </div>
      <div class="imports-custom">
        <input id="imports-input" class="console-input" placeholder="z. B. java.util.Scanner" />
        <button id="imports-add" class="tool-btn">Hinzufügen</button>
      </div>
      <div class="modal-actions"><button id="imports-close" class="tool-btn">Schließen</button></div>
    </div>`;
  overlay.addEventListener("click", (e) => { if (e.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);

  const addImport = (pkg: string): void => {
    const line = `import ${pkg};`;
    if (!editor.state.doc.toString().includes(line)) {
      editor.dispatch({ changes: { from: 0, insert: line + "\n" } });
    }
  };
  overlay.querySelectorAll<HTMLButtonElement>("[data-pkg]").forEach((b) =>
    b.addEventListener("click", () => { addImport(b.dataset.pkg!); overlay.remove(); }));
  const input = overlay.querySelector<HTMLInputElement>("#imports-input")!;
  const commit = (): void => {
    const v = input.value.trim().replace(/^import\s+/, "").replace(/;$/, "");
    if (v) addImport(v);
    overlay.remove();
  };
  overlay.querySelector<HTMLButtonElement>("#imports-add")!.addEventListener("click", commit);
  input.addEventListener("keydown", (e) => { if (e.key === "Enter") commit(); });
  overlay.querySelector<HTMLButtonElement>("#imports-close")!.addEventListener("click", () => overlay.remove());
  input.focus();
}
