# SimplestJavaIDE

A minimal Java IDE for teaching, built with Java Swing. Its goal: let students
**write and run Java without ever touching classes, objects or boilerplate**.
Originally built for use in German vocational schools (e.g. 11th grade,
*Fachoberschule Informationstechnik Hessen*).

## The idea

A program is just a set of **methods** plus a list of **imports**. The IDE hides
everything else. Behind the scenes it wraps the methods in a generated class,
compiles it, and runs it — the student only ever sees and edits the method
bodies, each in its own tab.

A project is stored in a `.sji` file (JSON) containing:

- `imports` — the `import` lines
- `methods` — each method's name and source
- `javaClass` — the name of the generated wrapper class

## Features

- Tabbed editor with Java syntax highlighting (RSyntaxTextArea) and a dark theme
  (FlatLaf).
- One-click **compile & run** with an interactive terminal (program input/output),
  plus a **Stop** button to end a running program (e.g. an endless loop).
- **Compiler errors are mapped back to the method tab and the local line** the
  student actually sees, and the offending line is highlighted — not the line of
  the hidden generated file. Runtime stack traces are annotated with the matching
  method and line too.
- Add methods via the **+** tab; rename/delete via right-click. Manage imports in
  a dedicated dialog.
- Keyboard shortcuts (save, run, zoom), an unsaved-changes marker and prompt,
  adjustable font size, and recently-opened files — all remembered between
  sessions.

## Requirements

- A **JDK 17 or newer** (the app compiles the student's code in-process via the
  JDK compiler API, so a plain JRE is not enough). Built and tested against
  Java 21 bytecode.

## Build

```bash
mvn clean package
```

This produces a single runnable jar at `target/SimplestJavaIDE.jar` bundling all
dependencies and resources.

## Run

```bash
java -jar target/SimplestJavaIDE.jar
```

On the start screen, create a new `.sji` program or open an existing one.

## Project layout

```
src/main/java/simplestJavaIDEpackage/
  StartingWindow.java        # entry point: new / open project
  MainUserInput.java         # main window, wires panels and toolbar actions
  Library/
    MethodTabsPanel.java     # the per-method editor tabs + error highlighting
    MethodManagerPanel.java  # add / delete methods
    CodingArea.java          # a single syntax-highlighted method editor
    TerminalPanel.java       # toolbar + terminal, drives compile & run
    AddImportsWindow.java    # manage imports
    CodeStructure/
      CodingFile.java        # the project model + source generation
      GeneratedSource.java   # generated code + line map (generated -> method)
      Methods.java, Classes.java
      FileManager.java       # JSON load / save (Gson)
    Commands/
      JavaCompilerService.java  # in-process compilation + diagnostics
      Runner.java, StreamReader.java  # run the program in a separate process
```

## Tests

```bash
mvn test
```

Covers source-line mapping, compiler-error mapping, and JSON round-tripping.

## License / credits

© Daniel Trageser. Bundles RSyntaxTextArea and FlatLaf — see the in-app
*Help/Imprint* dialog for their license notices.
