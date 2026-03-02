---
name: review-logic
description: 審查 img2pdf-gui 的程式邏輯正確性，輸出 Markdown 格式問題報告
allowed-tools: Read, Grep, Glob
disable-model-invocation: true
---

你將對 img2pdf-gui 專案進行 **程式邏輯正確性** 的唯讀審查。

**嚴格禁止**：不得使用 Edit、Write、Bash 工具，不得修改任何檔案。

---

## 檢查點

- **空指標風險**：未經 null 檢查的物件存取、可能回傳 null 的方法呼叫
- **資源洩漏**：`InputStream`、`OutputStream`、`ExecutorService` 等是否在 finally 或 try-with-resources 中關閉
- **邊界條件**：空陣列、空清單、空字串、null 輸入的處理
- **整數溢出、路徑拼接錯誤、格式化例外**

---

## 輸出格式

以 Markdown 呈現，每個問題使用以下模板：

```
### [邏輯] 問題標題

- **位置**：`檔案路徑:行號`
- **問題**：具體描述問題是什麼
- **建議方向**：改善方向（不提供修改後的程式碼）
- **嚴重度**：高 / 中 / 低
```

節末提供**小結**，列出此面向的整體評估（良好 / 待改善 / 需修正）。

---

## 絕對限制

- **只讀不改**：本技能為純審查，禁止提出具體程式碼修改。
- **不猜測程式行為**：有疑問時以 Grep 搜尋確認，不憑印象判斷。
