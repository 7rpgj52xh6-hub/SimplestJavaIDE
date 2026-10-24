import { runJava, type RunOutcome } from "../src/interpreter/run";
import type { Controller, IO } from "../src/interpreter/io";

async function run(src: string, input: string[] = []): Promise<{ out: string; outcome: RunOutcome }> {
  let out = "";
  const queue = [...input];
  const io: IO = {
    print: (t) => { out += t; },
    readLine: async () => (queue.length ? queue.shift()! : null),
  };
  const ctrl: Controller = { stopped: false };
  const outcome = await runJava(src, io, ctrl);
  return { out, outcome };
}

const main = (body: string) => `public static void main(String[] args){\n${body}\n}`;

let pass = 0;
let fail = 0;

async function expectOut(name: string, src: string, expected: string, input: string[] = []): Promise<void> {
  const { out, outcome } = await run(src, input);
  if (out === expected && (outcome.status === "ok")) { pass++; return; }
  fail++;
  console.log(`FAIL ${name}`);
  console.log(`  expected: ${JSON.stringify(expected)}`);
  console.log(`  got:      ${JSON.stringify(out)}`);
  console.log(`  outcome:  ${JSON.stringify(outcome)}`);
}

async function expectStatus(name: string, src: string, status: RunOutcome["status"], input: string[] = []): Promise<void> {
  const { outcome } = await run(src, input);
  if (outcome.status === status) { pass++; return; }
  fail++;
  console.log(`FAIL ${name}: expected status ${status}, got ${JSON.stringify(outcome)}`);
}

async function main_() {
  await expectOut("hello", main(`System.out.println("Hello World");`), "Hello World\n");
  await expectOut("int-div", main(`System.out.println(5/2);`), "2\n");
  await expectOut("double-div", main(`System.out.println(5.0/2);`), "2.5\n");
  await expectOut("double-print", main(`System.out.println(2.0);`), "2.0\n");
  await expectOut("concat", main(`int x=3; System.out.println("x=" + x);`), "x=3\n");
  await expectOut("print-noln", main(`System.out.print("a"); System.out.print("b");`), "ab");
  await expectOut("if-else", main(`int a=5; if(a>3) System.out.println("gross"); else System.out.println("klein");`), "gross\n");
  await expectOut("while", main(`int i=0; while(i<3){ System.out.println(i); i++; }`), "0\n1\n2\n");
  await expectOut("for", main(`for(int i=0;i<3;i++) System.out.print(i);`), "012");
  await expectOut("do-while", main(`int i=0; do { System.out.print(i); i++; } while(i<3);`), "012");
  await expectOut("nested-loop", main(`for(int i=1;i<=2;i++){ for(int j=1;j<=2;j++){ System.out.print(i*j+" "); } }`), "1 2 2 4 ");
  await expectOut("ternary", main(`int a=4; System.out.println(a%2==0 ? "gerade" : "ungerade");`), "gerade\n");
  await expectOut("bool-logic", main(`boolean b = (3>2) && (1<0 || 5==5); System.out.println(b);`), "true\n");
  await expectOut("char", main(`char c='A'; System.out.println(c); System.out.println(c+1);`), "A\n66\n");
  await expectOut("string-methods", main(`String s="Hallo"; System.out.println(s.length() + " " + s.toUpperCase() + " " + s.charAt(1));`), "5 HALLO a\n");
  await expectOut("math", main(`System.out.println(Math.max(3,7) + " " + Math.abs(-4));`), "7 4\n");
  await expectOut("array-literal", main(`int[] a={1,2,3}; int s=0; for(int x:a) s+=x; System.out.println(s);`), "6\n");
  await expectOut("array-new", main(`int[] a=new int[3]; a[0]=5; a[2]=9; System.out.println(a[0] + "," + a[2] + "," + a.length);`), "5,9,3\n");
  await expectOut("break-continue", main(`for(int i=0;i<10;i++){ if(i==2) continue; if(i==4) break; System.out.print(i); }`), "013");
  await expectOut(
    "method-recursion",
    `static int fak(int n){ if(n<=1) return 1; return n*fak(n-1); }\n` + main(`System.out.println(fak(5));`),
    "120\n",
  );
  await expectOut(
    "method-params",
    `static int add(int a, int b){ return a+b; }\n` + main(`System.out.println(add(20, 22));`),
    "42\n",
  );
  await expectOut("scanner-line", main(`Scanner sc=new Scanner(System.in); String n=sc.nextLine(); System.out.println("Hallo " + n);`), "Hallo Welt\n", ["Welt"]);
  await expectOut("scanner-int", main(`Scanner sc=new Scanner(System.in); int a=sc.nextInt(); System.out.println(a*2);`), "42\n", ["21"]);
  await expectOut("scanner-two-ints", main(`Scanner sc=new Scanner(System.in); int a=sc.nextInt(); int b=sc.nextInt(); System.out.println(a+b);`), "7\n", ["3 4"]);
  await expectStatus("div-zero", main(`System.out.println(5/0);`), "runtimeError");
  await expectStatus("parse-error", main(`int x = ;`), "compileError");
  await expectStatus("unknown-var", main(`System.out.println(y);`), "runtimeError");
  await expectStatus("no-main", `static int f(){ return 1; }`, "runtimeError");

  // ---- newer features ----
  await expectOut("cast-int", main(`double d=3.9; int x=(int)d; System.out.println(x);`), "3\n");
  await expectOut("cast-char", main(`int a=65; char c=(char)a; System.out.println(c);`), "A\n");
  await expectOut("arraylist", main(`ArrayList<Integer> l=new ArrayList<>(); l.add(1); l.add(2); l.add(3); System.out.println(l.size() + " " + l.get(1) + " " + l);`), "3 2 [1, 2, 3]\n");
  await expectOut("list-foreach", main(`ArrayList<String> l=new ArrayList<>(); l.add("a"); l.add("b"); String r=""; for(String s : l) r += s; System.out.println(r);`), "ab\n");
  await expectOut("printf", main(`System.out.printf("%d + %d = %d%n", 2, 3, 2+3);`), "2 + 3 = 5\n");
  await expectOut("format-f", main(`System.out.println(String.format("%.2f", 3.14159));`), "3.14\n");
  await expectOut("character", main(`System.out.println(Character.isDigit('5') + " " + Character.toUpperCase('a'));`), "true A\n");
  await expectOut("string-split", main(`String[] parts = "a,b,c".split(","); System.out.println(parts.length + " " + parts[1]);`), "3 b\n");
  await expectOut("random-bounded", main(`Random r=new Random(); System.out.println(r.nextInt(1));`), "0\n");

  // ---- classes / objects ----
  await expectOut("class-konto",
    `public class Konto {\n  double stand;\n  Konto(double start){ this.stand = start; }\n  void einzahlen(double b){ this.stand = this.stand + b; }\n  double getStand(){ return this.stand; }\n}\n` +
    main(`Konto k = new Konto(100); k.einzahlen(50); System.out.println(k.getStand());`),
    "150.0\n");
  await expectOut("class-fields-bare",
    `class Punkt {\n  int x;\n  int y;\n  Punkt(int x, int y){ this.x = x; this.y = y; }\n  int summe(){ return x + y; }\n}\n` +
    main(`Punkt p = new Punkt(3, 4); System.out.println(p.summe() + " " + p.x);`),
    "7 3\n");
  await expectOut("class-only-main",
    `public class Haupt {\n  public static void main(String[] args){\n    int[] a = {5,3,8}; int max = a[0];\n    for (int x : a) if (x > max) max = x;\n    System.out.println("Max: " + max);\n  }\n}`,
    "Max: 8\n");
  await expectOut("class-two-objects",
    `class Zaehler { int n = 0; void hoch(){ n++; } int wert(){ return n; } }\n` +
    main(`Zaehler a = new Zaehler(); Zaehler b = new Zaehler(); a.hoch(); a.hoch(); b.hoch(); System.out.println(a.wert() + " " + b.wert());`),
    "2 1\n");

  console.log(`\n${pass} passed, ${fail} failed`);
  if (fail > 0) process.exitCode = 1;
}

void main_();
