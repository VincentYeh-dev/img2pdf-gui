# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile
mvn compile

# Package (creates fat JAR + Windows EXE)
mvn clean package
```

Output artifacts in `target/`:
- `target-img2pdf.gui-1.0.3.jar` — shaded fat JAR (main class: `org.vincentyeh.img2pdf.gui.App`)
- `target/bin/img2pdf-gui 1.0.3.exe` — Windows executable wrapper via launch4j

**No test suite exists.** There are no tests configured in this project.

## Architecture

This is a Java 8 Swing desktop application for batch image-to-PDF conversion, built with a strict **MVC + Mediator** pattern.

### Entry Point
`App.java` — initializes FlatDarkLaf theme, instantiates Model/View/Controller, and wires them together via a `JFrame`.

### Model (`model/`)
- **`Model.java`** — core business logic. `parseSourceFiles()` applies glob filtering, numeric sorting, and pattern-based filename formatting to produce a list of `Task` objects. `convert()` runs batch PDF generation on a background thread using the external `img2pdf-lib` dependency.
- **`Task.java`** — POJO: `destination` (output PDF path) + `files` (image files to convert).
- **`ModelListener`** — callback interface the Controller implements to receive progress/log events from the Model.
- **`model/util/file/`** — three key utilities:
  - `FileNameFormatter.java` — pattern substitution for output PDF names. Supports `<NAME>`, `<PARENT{n}>`, current-time tokens (`<CY>/<CM>/<CD>/...`), and file-modification-time tokens (`<MY>/<MM>/<MD>/...`).
  - `FileSorter.java` — numeric/alphanumeric ascending/descending sort.
  - `GlobbingFileFilter.java` — glob pattern filtering (e.g. `*.jpg`, `*.{png,jpg}`).

### View (`view/`)
- **`View.java` / `View.form`** — IntelliJ UI Designer–generated Swing layout. Do not hand-edit `View.java` directly; use the `.form` designer.
- **`UIState.java`** — singleton holding all current GUI values (source files, page size/alignment/direction, encryption, color type, file filter pattern, destination folder).
- **`UIMediator` / `JUIMediator.java`** — Mediator pattern hub that wires all Swing components together and exposes a `Builder` API. `JUIMediator` is ~600 lines and manages all UI state transitions and component interactions.
- **`MediatorListener`** — interface Controller implements to receive UI events (`onSourcesUpdate`, `onConvertButtonClick`, `onStopButtonClick`).

### Controller (`controller/`)
- **`Controller.java`** — implements both `MediatorListener` and `ModelListener`. Translates UI events into Model calls and relays Model progress/log events back to the UI via `JUIMediator`.

### Data Flow
1. User configures source folders and options → `UIState` updated → `MediatorListener.onSourcesUpdate()` fires.
2. Controller calls `Model.parseSourceFiles()` → returns `Task[]` → Controller updates task list in UI.
3. User clicks Convert → `MediatorListener.onConvertButtonClick()` fires → Controller calls `Model.convert()`.
4. Model runs conversion on background thread, firing `ModelListener` progress/log callbacks → Controller relays to `JUIMediator`.

## Key Dependencies

- **`img2pdf.lib` (8.0.1)** — proprietary/custom library for the actual PDF generation; not on Maven Central.
- **`flatlaf` (1.6.1)** — FlatDarkLaf Swing look-and-feel.
- **`forms_rt` (7.0.3)** — IntelliJ UI Designer runtime (required for `View.java`).
