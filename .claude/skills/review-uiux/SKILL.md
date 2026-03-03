---
name: review-uiux
description: 審查 img2pdf-gui 的 UI/UX 設計品質，輸出 Markdown 格式問題報告
allowed-tools: Read, Grep, Glob
disable-model-invocation: true
---

你將對 img2pdf-gui 專案進行 **UI/UX** 的唯讀審查。

**嚴格禁止**：不得使用 Edit、Write、Bash 工具，不得修改任何檔案。

---

## 檢查點

- **元件命名**：每個互動元件是否透過 `setName()` 設定名稱（影響 AssertJ Swing 測試可靠性）
- **使用者回饋**：長時間操作是否有進度提示？錯誤是否有明確訊息？
- **狀態一致性**：按鈕啟用/停用、欄位可編輯性是否與程式狀態同步？
- **無障礙**：是否設定 `setToolTipText`、`setAccessibleName` 等輔助說明？

---

## 輸出格式

以 Markdown 呈現，每個問題使用以下模板：

```
### [UI/UX] 問題標題

- **位置**：`檔案路徑:行號`
- **問題**：具體描述問題是什麼
- **建議方向**：改善方向（不提供修改後的程式碼）
- **嚴重度**：高 / 中 / 低
```

節末提供**小結**，列出此面向的整體評估（良好 / 待改善 / 需改善）。

---

## 絕對限制

- **只讀不改**：本技能為純審查，禁止提出具體程式碼修改。
- **不猜測程式行為**：有疑問時以 Grep 搜尋確認，不憑印象判斷。
