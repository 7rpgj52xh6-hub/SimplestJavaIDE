import {
  snippetCompletion,
  type Completion,
  type CompletionContext,
  type CompletionResult,
} from "@codemirror/autocomplete";

const KEYWORDS = [
  "public", "private", "protected", "static", "final", "void", "int", "double",
  "boolean", "char", "long", "String", "if", "else", "while", "for", "do",
  "return", "break", "continue", "new", "true", "false", "null", "class",
  "import", "var",
];

const API: Completion[] = [
  { label: "System.out.println", type: "function", info: "Gibt eine Zeile in der Konsole aus" },
  { label: "System.out.print", type: "function", info: "Ausgabe ohne Zeilenumbruch" },
  { label: "System.out.printf", type: "function", info: "Formatierte Ausgabe, z. B. %d, %.2f" },
  { label: "Scanner", type: "class", info: "Eingabe lesen: new Scanner(System.in)" },
  { label: "Integer.parseInt", type: "function", info: "Text → ganze Zahl" },
  { label: "Double.parseDouble", type: "function", info: "Text → Kommazahl" },
  { label: "Math.max", type: "function" }, { label: "Math.min", type: "function" },
  { label: "Math.abs", type: "function" }, { label: "Math.sqrt", type: "function" },
  { label: "Math.pow", type: "function" }, { label: "Math.random", type: "function" },
  { label: "Math.round", type: "function" }, { label: "Math.PI", type: "constant" },
  { label: "ArrayList", type: "class", info: "Dynamische Liste: new ArrayList<>()" },
  { label: "Random", type: "class", info: "Zufallszahlen: new Random()" },
  { label: "length", type: "property", info: "Länge eines Arrays" },
  { label: "length()", type: "method", info: "Länge eines Strings" },
  { label: "charAt", type: "method" }, { label: "substring", type: "method" },
  { label: "equals", type: "method" }, { label: "toUpperCase", type: "method" },
  { label: "toLowerCase", type: "method" }, { label: "indexOf", type: "method" },
  { label: "nextLine", type: "method" }, { label: "nextInt", type: "method" },
  { label: "nextDouble", type: "method" },
];

const SNIPPETS: Completion[] = [
  snippetCompletion('System.out.println(${});', { label: "sout", type: "keyword", detail: "println", info: "Ausgabe einer Zeile" }),
  snippetCompletion('System.out.print(${});', { label: "sop", type: "keyword", detail: "print" }),
  snippetCompletion('for (int ${i} = 0; ${i} < ${n}; ${i}++) {\n\t${}\n}', { label: "fori", type: "keyword", detail: "for-Schleife" }),
  snippetCompletion('while (${bedingung}) {\n\t${}\n}', { label: "while", type: "keyword" }),
  snippetCompletion('if (${bedingung}) {\n\t${}\n}', { label: "if", type: "keyword" }),
  snippetCompletion('if (${bedingung}) {\n\t${}\n} else {\n\t${}\n}', { label: "ifelse", type: "keyword" }),
  snippetCompletion('public static void main(String[] args) {\n\t${}\n}', { label: "main", type: "keyword", detail: "main-Methode" }),
  snippetCompletion('Scanner ${sc} = new Scanner(System.in);', { label: "scanner", type: "keyword", detail: "Eingabe" }),
  snippetCompletion('ArrayList<${Integer}> ${liste} = new ArrayList<>();', { label: "arraylist", type: "keyword" }),
];

const OPTIONS: Completion[] = [
  ...SNIPPETS,
  ...KEYWORDS.map((k): Completion => ({ label: k, type: "keyword" })),
  ...API,
];

/** Completions for the teaching API, keywords and common snippets. */
export function javaCompletions(context: CompletionContext): CompletionResult | null {
  const word = context.matchBefore(/[\w.]*/);
  if (!word || (word.from === word.to && !context.explicit)) return null;
  return { from: word.from, options: OPTIONS, validFor: /^[\w.]*$/ };
}
