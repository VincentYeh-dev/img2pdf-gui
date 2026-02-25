# CLAUDE.md

此檔案為 Claude Code (claude.ai/code) 在此專案中運作時的指引。

---

## 1. 開場白

本文件定義 Claude Code 在此專案中的行為規範、架構認識與操作限制。
專案為批次圖片轉 PDF 的 Windows 桌面應用程式，採用 Java 8 Swing 開發。

---

## 2. 專案概覽

- **應用程式名稱**：img2pdf-gui v1.0.3
- **功能**：批次將圖片轉換為 PDF 的 Windows 桌面應用
- **平台**：Windows 11 / Java 8 Swing
- **主類別**：`org.vincentyeh.img2pdf.gui.App`
- **無測試套件**：每次任務後必須以 `mvn compile` 手動驗證。

---

## 3. 技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Java | 8 | OpenJDK |
| Swing | — | 桌面 UI 框架 |
| Maven | — | 建置工具 |
| FlatLaf | 1.6.1 | FlatDarkLaf 深色主題 |
| img2pdf.lib | 8.0.1 | PDF 生成（私有庫，非 Maven Central） |
| forms_rt | 7.0.3 | IntelliJ UI Designer runtime |
| launch4j plugin | 2.1.1 | 產生 Windows EXE |
| maven-shade plugin | 3.2.4 | 產生 Fat JAR |

---

## 4. 架構說明

本專案採用嚴格的 **MVC + Mediator** 架構模式。

原始碼根目錄：`src/main/java/org/vincentyeh/img2pdf/gui/`

### 進入點
- **`App.java`** — 初始化 FlatDarkLaf 深色主題，實例化 Model/View/Controller，並透過 `JFrame` 將三者串接。
- **`Constants.java`** — 定義全域常數（如 `APP_TITLE`）。

### Model（`model/`）
- **`Model.java`** — 核心業務邏輯。`parseSourceFiles()` 依序套用 glob 過濾、數字排序、樣式化檔名格式化，產生 `Task` 物件清單。`convert()` 在背景執行緒呼叫外部 `img2pdf-lib` 進行批次 PDF 轉換。
- **`Task.java`** — POJO：`destination`（輸出 PDF 路徑）+ `files`（待轉換圖片檔案陣列）。
- **`ModelListener`** — Controller 實作的回呼介面，用於接收 Model 的進度與日誌事件。
- **`model/util/interfaces/NameFormatter.java`** — 檔名格式化介面。
- **`model/util/file/`** — 工具類別：
  - `FileNameFormatter.java` — 輸出 PDF 檔名的樣式替換，支援 `<NAME>`、`<PARENT{n}>`、當前時間標記（`<CY>/<CM>/<CD>/...`）及檔案修改時間標記（`<MY>/<MM>/<MD>/...`）。
  - `FileSorter.java` — 數字／英數字升冪或降冪排序。
  - `GlobbingFileFilter.java` — Glob 樣式檔案過濾（例如 `*.jpg`、`*.{png,jpg}`）。

### View（`view/`）
- **`View.java` / `View.form`** — `View.form` 為 IntelliJ UI Designer 佈局定義，`View.java` 為手動維護的對應實作。`View.java` 中所有被 `.form` 綁定（`binding`）的元件**必須宣告為 instance fields**，並在 `$$$setupUI$$$()` 中初始化後交由 `JUIMediator.Builder` 接管。
- **`UIState.java`** — 單例，儲存所有目前的 GUI 狀態（來源檔案、頁面大小／對齊／方向、加密、色彩類型、目標資料夾）。
- **`UIMediator` / `JUIMediator.java`** — Mediator 模式的核心，將所有 Swing 元件串接，並提供 `Builder` API，負責管理所有 UI 狀態切換與元件互動。
- **`MediatorListener`** — Controller 實作的介面，用於接收 UI 事件（`onSourcesUpdate`、`onConvertButtonClick`、`onStopButtonClick`）。

### Controller（`controller/`）
- **`Controller.java`** — 同時實作 `MediatorListener` 與 `ModelListener`，將 UI 事件轉換為 Model 呼叫，並將 Model 的進度與日誌事件回傳給 `JUIMediator` 更新畫面。

### 資料流
1. 使用者設定來源資料夾與選項 → `UIState` 更新 → 觸發 `MediatorListener.onSourcesUpdate()`。
2. Controller 呼叫 `Model.parseSourceFiles()` → 回傳 `Task[]` → Controller 更新 UI 的任務清單。
3. 使用者點擊轉換 → 觸發 `MediatorListener.onConvertButtonClick()` → Controller 呼叫 `Model.convert()`。
4. Model 在背景執行緒進行轉換，透過 `ModelListener` 回呼發送進度與日誌 → Controller 轉交 `JUIMediator` 更新畫面。

---

## 5. 必要指令

```bash
mvn compile          # 編譯驗證
mvn clean package    # 打包（Fat JAR + EXE）
```

輸出檔案位於 `target/`（版本由 `pom.xml` 的 `${project.version}` 決定）：
- `target/target-img2pdf-gui-{version}.jar` — 包含所有依賴的 Fat JAR（由 `${jar-name}` 屬性決定，前綴有 `target-`）
- `target/bin/img2pdf-gui {version}.exe` — 透過 launch4j 產生的 Windows 執行檔（由 `${exe-name}` 屬性決定）

---

## 6. 開發流程

### 分支模型
本專案採用簡化 Git Flow 分支模型。

- **`master`** — 正式發布版本，**唯讀**。禁止 Claude Code 直接 commit、push 或合併至此分支（可讀取／查看）。
- **`develop`** — 主要開發分支，所有功能開發完成後合併至此。
- **`feature/*`** — 從 `develop` 分出，功能完成後合併回 `develop`。
- **`hotfix/*`** — 從 `master` 分出，修復完成後合併回 `develop`（由 Claude Code 執行）；合併至 `master` 由**使用者手動執行**。合併完成後刪除該 hotfix 分支。

### Commit 規則
- 使用中文撰寫 commit message，只寫一行摘要，不加描述段落。
- 禁止對任何分支執行 force push（`--force` / `--force-with-lease`）。
- 每次任務完成後，主動執行 `mvn compile` 驗證，通過後直接 commit，無需詢問使用者。
- 執行任何 git 操作前，必須先確認目前所在的分支是否正確。

### 合併規則
- `feature/*` → `develop`
- `hotfix/*` → `develop`（Claude Code 執行）、`hotfix/*` → `master`（使用者手動執行）
- 只有 `develop` 和 `hotfix/*` 可以合併進 `master`。

### 依賴管理
**嚴禁**在未經使用者明確允許的情況下，於 `pom.xml` 新增任何 Maven 套件（`<dependency>`）或 Maven Plugin（`<plugin>`）。
若評估後認為需要引入新依賴或 Plugin，必須先向使用者說明原因與替代方案，待使用者明確同意後才可修改 `pom.xml`。

---

## 7. 重要文件連結

原始碼根目錄：`src/main/java/org/vincentyeh/img2pdf/gui/`

| 角色 | 路徑 |
|------|------|
| 進入點 | `src/main/java/org/vincentyeh/img2pdf/gui/App.java` |
| UI 佈局定義 | `src/main/java/org/vincentyeh/img2pdf/gui/view/View.form` |
| UI 佈局實作 | `src/main/java/org/vincentyeh/img2pdf/gui/view/View.java` |
| Mediator（UI 邏輯核心） | `src/main/java/org/vincentyeh/img2pdf/gui/view/JUIMediator.java` |
| GUI 狀態 | `src/main/java/org/vincentyeh/img2pdf/gui/view/UIState.java` |
| 業務邏輯 | `src/main/java/org/vincentyeh/img2pdf/gui/model/Model.java` |
| 協調層 | `src/main/java/org/vincentyeh/img2pdf/gui/controller/Controller.java` |
| 建置設定 | `pom.xml` |
| UI 優化文件 | `UI Optimization.md` |

---

## 8. 陷阱警告

- **View.form 綁定元件**：所有被 `.form` 綁定的元件必須宣告為 instance field，在 `$$$setupUI$$$()` 中初始化後才交由 `JUIMediator.Builder` 接管，否則會出現 `NullPointerException`。
- **img2pdf.lib 私有庫**：不在 Maven Central，需本地安裝至 local repository。不可從 `pom.xml` 刪除此依賴。
- **禁止未授權修改 pom.xml**：新增任何 `<dependency>` 或 `<plugin>` 前必須取得使用者明確同意。
- **master 分支唯讀**：禁止 Claude Code 直接 commit、push 或合併至 `master`。
- **無測試套件**：每次任務後必須以 `mvn compile` 手動驗證，無法依賴自動化測試。
