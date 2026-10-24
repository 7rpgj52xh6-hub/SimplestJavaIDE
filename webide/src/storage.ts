// Persists the current project in the browser (localStorage). Client-side only —
// the student's code never leaves their device.

export interface Project {
  code: string;
  savedAt: number;
}

const KEY = "simplestjavaide.project";

export function saveProject(code: string): void {
  try {
    localStorage.setItem(KEY, JSON.stringify({ code, savedAt: Date.now() } satisfies Project));
  } catch {
    /* storage may be unavailable (private mode) — ignore */
  }
}

export function loadProject(): Project | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Project) : null;
  } catch {
    return null;
  }
}
