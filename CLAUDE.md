# CLAUDE.md

此檔案為 Claude Code (claude.ai/code) 在此專案中運作時的指引。

## 環境
- Windows 11
- Open JDK 1.8.0

## 建置指令

```bash
# 編譯
mvn compile

# 打包（產生 fat JAR 與 Windows EXE）
mvn clean package
```

輸出檔案位於 `target/`：
- `target/img2pdf-gui-1.0.3.jar` — 包含所有依賴的 fat JAR（主類別：`org.vincentyeh.img2pdf.gui.App`）
- `target/bin/img2pdf-gui 1.0.3.exe` — 透過 launch4j 產生的 Windows 執行檔

**本專案無測試套件。**

## 架構

本專案為 Java 8 Swing 桌面應用程式，用於批次將圖片轉換為 PDF，採用嚴格的 **MVC + Mediator** 架構模式。

### 進入點
`App.java` — 初始化 FlatDarkLaf 深色主題，實例化 Model/View/Controller，並透過 `JFrame` 將三者串接。

### Model（`model/`）
- **`Model.java`** — 核心業務邏輯。`parseSourceFiles()` 依序套用 glob 過濾、數字排序、樣式化檔名格式化，產生 `Task` 物件清單。`convert()` 在背景執行緒呼叫外部 `img2pdf-lib` 進行批次 PDF 轉換。
- **`Task.java`** — POJO：`destination`（輸出 PDF 路徑）+ `files`（待轉換圖片檔案陣列）。
- **`ModelListener`** — Controller 實作的回呼介面，用於接收 Model 的進度與日誌事件。
- **`model/util/file/`** — 三個工具類別：
  - `FileNameFormatter.java` — 輸出 PDF 檔名的樣式替換，支援 `<NAME>`、`<PARENT{n}>`、當前時間標記（`<CY>/<CM>/<CD>/...`）及檔案修改時間標記（`<MY>/<MM>/<MD>/...`）。
  - `FileSorter.java` — 數字／英數字升冪或降冪排序。
  - `GlobbingFileFilter.java` — Glob 樣式檔案過濾（例如 `*.jpg`、`*.{png,jpg}`）。

### View（`view/`）
- **`View.java` / `View.form`** — `View.form` 為 IntelliJ UI Designer 佈局定義，`View.java` 為手動維護的對應實作。`View.java` 中所有被 `.form` 綁定（`binding`）的元件**必須宣告為 instance fields**，並在 `$$$setupUI$$$()` 中初始化後交由 `JUIMediator.Builder` 接管。
- **`UIState.java`** — 單例，儲存所有目前的 GUI 狀態（來源檔案、頁面大小／對齊／方向、加密、色彩類型、目標資料夾）。
- **`UIMediator` / `JUIMediator.java`** — Mediator 模式的核心，將所有 Swing 元件串接，並提供 `Builder` API。`JUIMediator` 約 600 行，負責管理所有 UI 狀態切換與元件互動。
- **`MediatorListener`** — Controller 實作的介面，用於接收 UI 事件（`onSourcesUpdate`、`onConvertButtonClick`、`onStopButtonClick`）。

### Controller（`controller/`）
- **`Controller.java`** — 同時實作 `MediatorListener` 與 `ModelListener`，將 UI 事件轉換為 Model 呼叫，並將 Model 的進度與日誌事件回傳給 `JUIMediator` 更新畫面。

### 資料流
1. 使用者設定來源資料夾與選項 → `UIState` 更新 → 觸發 `MediatorListener.onSourcesUpdate()`。
2. Controller 呼叫 `Model.parseSourceFiles()` → 回傳 `Task[]` → Controller 更新 UI 的任務清單。
3. 使用者點擊轉換 → 觸發 `MediatorListener.onConvertButtonClick()` → Controller 呼叫 `Model.convert()`。
4. Model 在背景執行緒進行轉換，透過 `ModelListener` 回呼發送進度與日誌 → Controller 轉交 `JUIMediator` 更新畫面。

## Key Dependencies

- **`img2pdf.lib` (8.0.1)** — proprietary/custom library for the actual PDF generation; not on Maven Central.
- **`flatlaf` (1.6.1)** — FlatDarkLaf Swing look-and-feel.
- **`forms_rt` (7.0.3)** — IntelliJ UI Designer runtime (required for `View.java`).

## Git Workflow

本專案採用簡化 Git Flow 分支模型。

### 分支模型
- **`master`** — 正式發布版本，**唯讀**。禁止直接 commit、push 或合併操作（可讀取／查看）。對 master 的任何合併操作需由使用者手動執行，Claude Code 不可自行操作。
- **`develop`** — 主要開發分支，所有功能開發完成後合併至此。
- **`feature/*`** — 從 `develop` 分出，功能完成後合併回 `develop`。
- **`hotfix/*`** — 從 `master` 分出，修復完成後同時合併回 `master` 與 `develop`，合併完成後刪除該 hotfix 分支。

### Commit 規則
- 使用中文撰寫 commit message。
- 禁止對任何分支執行 force push（`--force` / `--force-with-lease`）。

### 合併規則
- `feature/*` → `develop`
- `hotfix/*` → `master` + `develop`（由使用者手動合併至 master）
- 只有 `develop` 和 `hotfix/*` 可以合併進 `master`。
