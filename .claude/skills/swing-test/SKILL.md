---
name: swing-test
description: >
  為 img2pdf-gui 的 JUIMediator 新行為撰寫 AssertJ Swing 驗收測試，
  放入 JUIMediatorTest.java，並執行 mvn test 確認通過。
argument-hint: "[要測試的功能描述（可省略）]"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
---

你將為 img2pdf-gui 的 UI 行為撰寫 AssertJ Swing 驗收測試。

## 測試哲學

### 黑箱測試（優先）
- **從使用者角度出發**：測試描述「使用者操作後，UI 應有什麼合理反應」，而非「主程式目前如何實作」。
- **獨立判斷合理性**：不預設主程式的邏輯是正確的；若 UI 行為不合理，測試應 FAIL 並回報問題。
- **測試 FAIL 代表發現問題**：若主程式行為不符合使用者預期，修正主程式使其通過，而非降低測試標準。
- **方法名稱反映行為意圖**：例如 `whenEncryptionEnabled_passwordFieldShouldBeEditable()`，清楚描述情境與期望。

### 白箱測試（允許，但須判斷合理性）
- **允許存取內部狀態**：可 cast 為具體類別、存取 package-private 欄位或直接呼叫內部方法，不限於公開 API。
- **合理性判斷**：使用白箱手法前，須能回答「黑箱為何不夠？」。合理情境包括：
  - 內部邏輯複雜，黑箱無法有效覆蓋邊界條件
  - 需隔離外部依賴（搭配 mock）以單獨驗證某層邏輯
  - 公開 API 無法觀察到的內部狀態變化
- **不合理的白箱使用**：為了讓測試更容易通過而繞過真實行為驗證，視為降低測試標準，應避免。
- **所有測試方法必須加英文 `//` 目的註解**：說明此測試驗證了什麼行為，置於 `@Test` 上方。
- **白箱測試額外加 `[White-box]` 合理性說明**（英文）：說明使用了什麼白箱手法，以及為何黑箱不足以驗證。

```java
// Black-box example:
// Verifies that enabling encryption makes the password fields editable.
@Test
void check_encrypt_enables_password_fields() { ... }

// White-box example:
// Verifies that updateTaskStatus(task, true) records the task as SUCCESS in the status map.
// [White-box] Directly accesses JUIMediator.taskStatusMap.
// Justification: UIMediator interface has no public API to query per-task status;
// taskStatusMap is the sole data source for JTree icon rendering and cannot be observed externally.
@Test
void updateTaskStatus_success_marks_task_as_SUCCESS() { ... }
```

## 測試結構

- 測試檔：`src/test/java/org/vincentyeh/img2pdf/gui/view/JUIMediatorTest.java`
- 套件：`org.vincentyeh.img2pdf.gui.view`（package-private 存取 `UIState.resetForTesting()`）
- setUp 模式：

```java
@BeforeEach
void setUp() {
    UIState.resetForTesting();
    robot = BasicRobot.robotWithNewAwtHierarchy();
    view = GuiActionRunner.execute(() -> new View());
    mediator = view.getUIMediator();
    GuiActionRunner.execute(() -> mediator.initialize());
    JFrame frame = GuiActionRunner.execute(() -> {
        JFrame f = new JFrame();
        f.setContentPane(view.getRootPanel());
        f.pack();
        f.setVisible(true);
        return f;
    });
    window = new FrameFixture(robot, frame);
}

@AfterEach
void tearDown() {
    window.cleanUp();
}
```

## 必須遵守的陷阱規則

1. **JPasswordField** 使用 `textBox(name)`，不是 `passwordField(name)`（`JPasswordField` 繼承自 `JTextComponent`）
2. 每個 `link*()` 已呼叫 `setName()`；若新增元件需確保補上，否則 AssertJ Swing 找不到元件
3. 需要觸發多步 UI 狀態時，用 `GuiActionRunner.execute(() -> mediator.notifyUI(...))` 而非連續 robot click（避免 timing 問題）
4. `outputFolderField` 初始有值（`"."`），測試前需 `deleteText()` 再 `enterText()`
5. Windows glob 大小寫不敏感：`*.jpg` 會匹配 `photo.JPG`，勿誤判為過濾失敗

## Mockito 使用規則

- **允許使用 Mockito**：可透過 `mock()`、`when()`、`verify()` 等 API 建立假物件、設定回傳值、驗證互動。
- **適用情境**：
  - 隔離外部依賴（如 `Model`）以專注測試 UI 或 Controller 邏輯
  - 驗證 Controller 是否正確呼叫 Model 的特定方法（行為驗證）
  - 模擬 `ModelListener` 回呼以測試 UI 對進度／日誌事件的反應
- **合理性要求**：mock 應用於替換真實依賴，而非規避本應測試的主程式邏輯；若 mock 讓一個本應 FAIL 的行為通過，視為不合理。
- **依賴確認**：若 `pom.xml` 尚未加入 Mockito，使用前須先向使用者確認是否允許新增依賴。

## 撰寫流程

1. 閱讀目前的 `JUIMediatorTest.java`，了解現有測試的涵蓋範圍
2. 以使用者操作角度定義期望行為，獨立判斷該行為是否合理
3. 在測試檔適當位置新增測試方法，**不加 Section 標頭或編號前綴**；每個測試方法上方必須有英文目的註解，白箱測試還需加 `[White-box]` 合理性說明
4. 執行 `mvn compile && mvn test`
5. 若測試 FAIL：分析主程式行為是否不符合使用者預期，若是則修正主程式；PASS 後依 CLAUDE.md 規則 commit

## 執行驗證

```bash
mvn compile && mvn test
```

測試全數 PASS 才可 commit，FAIL 時回到步驟 3 修正，不單獨 commit。
