---
name: unit-test
description: >
  為 img2pdf-gui 的 Model 層和 Utility 類別撰寫 JUnit 5 Edge-case 單元測試，
  並執行 mvn test 確認通過。
argument-hint: "[要測試的類別或方法（可省略）]"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash
---

你將為 img2pdf-gui 的 Model 層與 Utility 類別撰寫 JUnit 5 Edge-case 單元測試。

## 測試哲學：Edge-case 優先

**核心目標**：主動找讓程式壞掉的輸入，而非驗證正常流程。

- Edge-case 優先：`null`、空字串、空陣列、非法值、邊界數字、特殊路徑字元
- **每個 `@Test` 只驗證一個邊界條件**（一個測試一個行為）
- 不重複現有測試已涵蓋的 case（先讀現有測試再設計新測試）
- 方法名稱清楚反映測試意圖，例如：`parseSourceFiles_withNullGlob_shouldThrowException()`

---

## 測試檔位置

```
src/test/java/org/vincentyeh/img2pdf/gui/
  model/
    ModelParseTest.java          — Model.parseSourceFiles() / convert() 整合測試
    util/file/
      FileNameFormatterTest.java — FileNameFormatter.format() 格式化邏輯
      FileSorterTest.java        — FileSorter 排序邏輯（NUMERIC/NAME INCREASE/DECREASE）
      GlobbingFileFilterTest.java — GlobbingFileFilter glob 樣式過濾
```

測試套件根目錄：`src/test/java/org/vincentyeh/img2pdf/gui/`

---

## 測試結構範本

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileNameFormatterTest {

    // Verifies that null input throws NullPointerException immediately.
    @Test
    void format_withNullInput_shouldThrowNullPointerException() {
        FileNameFormatter formatter = new FileNameFormatter("<NAME>");
        assertThrows(NullPointerException.class, () -> formatter.format(null));
    }

    // Verifies that an unknown pattern token causes FormatException with NotMappedPattern as cause.
    @Test
    void format_withUnknownToken_shouldThrowFormatExceptionWithNotMappedPatternCause() {
        FileNameFormatter formatter = new FileNameFormatter("<UNKNOWN>");
        FormatException ex = assertThrows(FormatException.class, () -> formatter.format(someFile));
        assertInstanceOf(NotMappedPattern.class, ex.getCause());
    }
}
```

### 注意事項（來自過去測試經驗）

1. **`NotMappedPattern` wrapping**：`FileNameFormatter.format()` 將 `NotMappedPattern`（繼承自 `IllegalArgumentException`）包裝在 `FormatException` 中；斷言應使用 `assertThrows(FormatException.class, ...)` 再 `assertInstanceOf(NotMappedPattern.class, ex.getCause())`
2. **Windows glob 大小寫**：`PathMatcher` glob 在 Windows 不區分大小寫，`*.jpg` 可匹配 `photo.JPG`，勿誤判為過濾失敗
3. **`Model.parseSourceFiles()` 空目錄行為**：即使目錄中無檔案，也會產生一個含 0 個檔案的 Task；空目錄不等於 0 個 Task

---

## 常見 Edge-case 分類

| 類型 | 適用類別 | 範例輸入 |
|------|----------|----------|
| Null 參數 | 所有類別 | `null` 路徑、`null` pattern |
| 空字串 | FileNameFormatter | `""` 作為 pattern |
| 空陣列/集合 | FileSorter | 空的 `File[]` |
| 非法 glob | GlobbingFileFilter | `"[invalid"` |
| 邊界數字 | FileSorter | 只有一個元素的陣列 |
| 特殊路徑字元 | FileNameFormatter、GlobbingFileFilter | 含空格、含 `#`、含 Unicode |
| 混合大小寫 | GlobbingFileFilter | `*.JPG` 與 `.jpg` 的匹配 |
| 超長字串 | FileNameFormatter | 超過 255 字元的路徑 |

---

## 撰寫流程

1. **閱讀現有測試檔**：確認已涵蓋的 case，避免重複
2. **閱讀目標原始碼**：理解邊界條件與例外處理邏輯
3. **設計 Edge-case 清單**：列出尚未測試的邊界條件
4. **撰寫測試**：每個 `@Test` 驗證一個邊界條件，方法名稱清楚描述情境
5. **執行驗證**：

```bash
mvn compile && mvn test
```

6. **回報結果**：
   - **PASS**：列出新增的測試與涵蓋的 Edge-cases
   - **FAIL**：回報發現的 Bug（哪個方法、哪個邊界條件、實際 vs 預期），不自行修改 production code

---

## 執行驗證

```bash
mvn compile && mvn test
```

測試全數 PASS 才可結束，FAIL 時回報 Bug 給 Developer 修正。
