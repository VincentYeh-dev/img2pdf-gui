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
- **測試套件**：已加入 AssertJ Swing 測試作為 Vibe Coding 驗收工具，涵蓋單元測試與 GUI 自動化測試。

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
| JUnit 5 | 5.9.3 | 測試框架（test scope） |
| AssertJ Swing | 3.17.1 | Swing GUI 自動化測試（test scope） |
| maven-surefire-plugin | 2.22.2 | JUnit 5 測試執行支援 |

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
- **`View.java` / `View.form`** — `View.form` 為 IntelliJ UI Designer 佈局定義，`View.java` 為手動維護的對應實作。`View.java` 中所有被 `.form` 綁定（`binding`）的元件**必須宣告為 instance fields**，並在 `$$$setupUI$$$()` 中初始化後交由 `JUIMediator.Builder` 接管。IntelliJ GUI Designer 設定為「儲存時產生 Java 原始碼」，因此 `$$$setupUI$$$()` 為**自動產生**，不可手動編輯。修改 UI 佈局時，**必須同步更改 `View.form` 與 `View.java`**：在 `View.form` 中調整佈局（IntelliJ 儲存時自動更新 `$$$setupUI$$$()`），同時在 `View.java` 手動同步以下部分：
  - 新增元件的 instance field 宣告
  - `JUIMediator.Builder` 的 `link*()` 呼叫
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
mvn test             # 執行所有測試（單元 + GUI）
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
- Commit 由**主 Agent 統一執行**，時機為 Developer → Code Reviewer → Tester 三個 sub-agent 全部通過後。
- **Sub-agent（developer、tester）禁止自行 commit**，僅回報結果給主 Agent。
- 執行任何 git 操作前，必須先確認目前所在的分支是否正確。

### 合併規則
- `feature/*` → `develop`
- `hotfix/*` → `develop`（Claude Code 執行）、`hotfix/*` → `master`（使用者手動執行）
- 只有 `develop` 和 `hotfix/*` 可以合併進 `master`。

### Vibe Coding 評估流程

依需求類型選擇對應流程，由主 Agent 依序調用三個 sub-agent：

#### 流程 A：新增功能（Feature）

觸發條件：使用者要求新增功能、新增 UI 元件、擴充現有邏輯等。

```
使用者描述需求
    ↓
[主 Agent — Plan 階段]
  探索程式碼，向使用者詳細詢問需求細節與設計選項
  （如：架構方式、UI 配置、參數設計等），待使用者確認後繼續
    ↓
從 develop 建立 feature/[功能名稱] 分支
    ↓
[developer sub-agent]     依確認的需求實作功能 → mvn compile 驗證（不 commit）
    ↓
[code-reviewer sub-agent] 唯讀審查（MVC / SOLID / 執行緒 / 邏輯 / Style / UI/UX） → 如果程式有嚴重違反規則，回到 developer 修正 → 循環
    ↓
[tester sub-agent]        撰寫 Edge-case 測試 → mvn test
    ↓
全部通過 → 主 Agent 統一 commit（實作 + 測試）
    ↑
如有 Bug → 回到 developer 修正 → 循環
```

#### 流程 B：檢查／修復（Inspect / Fix）

觸發條件：使用者要求「檢查程式」、「找 Bug」、「修復問題」、「重構」等。

```
使用者描述需求
    ↓
[code-reviewer sub-agent] 唯讀審查，列出所有問題（Major / Minor 分級）
    ↓
[主 Agent — 決策階段]
  將審查結果整理後呈現給使用者，詢問哪些問題需要修復
  （可提供選項讓使用者逐一決定），待使用者確認修復範圍後繼續
    ↓
[developer sub-agent]     依使用者確認的修復範圍修正問題 → mvn compile 驗證（不 commit）
    ↓
[tester sub-agent]        針對修復點撰寫 Edge-case 測試 → mvn test
    ↓
全部通過 → 主 Agent 統一 commit（修復 + 測試）
    ↑
如有新問題 → 回到 developer 修正 → 循環
```

#### Sub-agent 職責分工

| Sub-agent | 工具 | 職責 | 禁止 |
|-----------|------|------|------|
| `developer` | Read, Edit, Write, Bash(`mvn compile`), Glob, Grep | 依需求實作功能或修復 Bug；**所有程式碼註解必須使用英文** | `mvn test`、任何 git 寫入指令（`git add/commit/checkout/merge` 等，唯讀如 `git log/diff/status` 可用） |
| `code-reviewer` | Read, Grep, Glob | 唯讀審查 MVC / SOLID / 執行緒 / 邏輯 / Style / UI/UX；**審查時標記非英文註解** | 修改任何檔案；任何 git 寫入指令 |
| `tester` | Read, Edit, Write, Bash(`mvn compile`/`test`), Glob, Grep | Edge-case 破壞性測試（GUI + 單元）；**所有測試程式碼的註解必須使用英文** | 修改 production code；任何 git 寫入指令（`git add/commit/checkout/merge` 等，唯讀如 `git log/diff/status` 可用） |

#### 主 Agent 調用規則

1. **收到需求後，先判斷流程類型**：
   - 新增功能 → 流程 A：探索程式碼後向使用者詢問需求細節與選項，確認後從 `develop` 建立 `feature/[功能名稱]` 新分支，再調用 `developer`。
   - 檢查／修復 → 流程 B：保持在目前所在分支，先調用 `code-reviewer`，審查完成後將結果呈現給使用者並詢問修復範圍，確認後再調用 `developer`。
2. 依對應流程順序調用 sub-agent。
3. **全部通過後，由主 Agent 執行 commit**（包含實作與測試）。
4. 若任一環節回報 Bug 或審查問題 → **回到 `developer` sub-agent 修正** → 重新循環。

#### 評估結果

- **PASS**：Developer + Reviewer + Tester 全部通過 → 主 Agent commit
- **FAIL**：任一失敗 → 主 Agent 回到 Developer 修正，不單獨 commit

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
| GUI 測試 | `src/test/java/org/vincentyeh/img2pdf/gui/view/JUIMediatorTest.java` |
| Model 測試 | `src/test/java/org/vincentyeh/img2pdf/gui/model/ModelParseTest.java` |

---

## 8. 陷阱警告

- **View.form 綁定元件**：所有被 `.form` 綁定的元件必須宣告為 instance field，在 `$$$setupUI$$$()` 中初始化後才交由 `JUIMediator.Builder` 接管，否則會出現 `NullPointerException`。
- **img2pdf.lib 私有庫**：不在 Maven Central，需本地安裝至 local repository。不可從 `pom.xml` 刪除此依賴。存取方式：優先以 `Read` 工具直接讀取原始碼（`C:\Users\vince\IdeaProjects\img2pdf-lib\src\`）；原始碼不足時，允許使用 `javap`、`jar -tf` 對 img2pdf-lib 的 JAR 進行反編譯，**不需詢問使用者**。
- **第三方 Maven 依賴（如 mockito、junit）**：不得使用 `javap`、`jar -tf`、`java -jar` 等指令反編譯或執行 JAR。如需了解第三方函式庫 API，應查閱官方文件，或在測試程式碼中直接嘗試編譯驗證。
- **禁止未授權修改 pom.xml**：新增任何 `<dependency>` 或 `<plugin>` 前必須取得使用者明確同意。
- **master 分支唯讀**：禁止 Claude Code 直接 commit、push 或合併至 `master`。
- **JPasswordField 查找**：`FrameFixture` 無 `passwordField(name)` 方法，應改用 `textBox(name)`（`JPasswordField` 繼承自 `JTextComponent`）。
- **Windows glob 大小寫**：`PathMatcher` glob 在 Windows 不區分大小寫，`*.jpg` 可匹配 `photo.JPG`，測試時勿誤判為過濾失敗。
- **測試套件包名**：GUI 測試必須放在 `org.vincentyeh.img2pdf.gui.view` 套件，才能存取 `UIState.resetForTesting()`（package-private）。
- **元件命名**：`JUIMediator.Builder` 的每個 `link*()` 方法必須呼叫 `setName()`，AssertJ Swing 才能透過名稱找到元件。
- **`$$$setupUI$$$()` 為自動產生，禁止手動編輯**：IntelliJ GUI Designer 設定為「form 儲存時產生 Java 原始碼」，`$$$setupUI$$$()` 由 IntelliJ 自動寫入 `View.java`。手動修改 `$$$setupUI$$$()` 將在下次儲存 `View.form` 時被覆蓋。修改 UI 佈局時，**必須同步更改 `View.form` 與 `View.java`**：`View.form` 負責佈局定義（IntelliJ 儲存時自動同步 `$$$setupUI$$$()`），`View.java` 仍需手動維護：instance field 宣告、`JUIMediator.Builder` 的 `link*()` 呼叫。
- **程式碼註解語言**：所有 Java 原始碼（production code 與 test code）的註解（`//`、`/* */`、`/** */` Javadoc）**只能使用英文**。非英文註解屬 Coding Style 違規，code-reviewer 應標記為 Minor 問題。
