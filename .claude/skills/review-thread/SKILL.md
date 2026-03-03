---
name: review-thread
description: 審查 img2pdf-gui 的執行緒安全性，輸出 Markdown 格式問題報告
allowed-tools: Read, Grep, Glob
disable-model-invocation: true
---

你將對 img2pdf-gui 專案進行 **執行緒安全** 的唯讀審查。

**嚴格禁止**：不得使用 Edit、Write、Bash 工具，不得修改任何檔案。

---

## 檢查點

### Race Condition
- 多個執行緒是否存取共享可變狀態（shared mutable state）而未加同步保護？
- `volatile`、`AtomicXxx`、`synchronized` 是否使用正確且充分？
- 複合操作（read-check-write、read-modify-write）是否已原子化？
- `ExecutorService` 的 `Future` 是否被正確處理（避免忽略執行結果或例外）？

### Deadlock
- 是否存在多個鎖的嵌套獲取，且不同執行緒以不同順序獲取（鎖順序不一致）？
- `synchronized` 方法之間是否有循環依賴？
- `ExecutorService.submit()` 內是否再次呼叫 `submit()` 並等待回傳（可能耗盡 thread pool）？

### Swing EDT 規則
- 背景執行緒是否直接讀寫 Swing 元件？（應透過 `SwingUtilities.invokeLater`）
- EDT 是否執行耗時 I/O 或計算？（應移至背景執行緒）
- `ModelListener` 回呼是否確保在 EDT 上執行後才更新 UI？

---

## 輸出格式

以 Markdown 呈現，每個問題使用以下模板：

```
### [執行緒] 問題標題

- **位置**：`檔案路徑:行號`
- **問題**：具體描述問題是什麼
- **建議方向**：改善方向（不提供修改後的程式碼）
- **嚴重度**：高 / 中 / 低
```

節末提供**小結**，列出此面向的整體評估（良好 / 待改善 / 有風險）。

---

## 絕對限制

- **只讀不改**：本技能為純審查，禁止提出具體程式碼修改。
- **不猜測程式行為**：有疑問時以 Grep 搜尋確認，不憑印象判斷。
