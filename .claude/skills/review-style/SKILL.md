---
name: review-style
description: 審查 img2pdf-gui 的 Coding Style，輸出 Markdown 格式問題報告
allowed-tools: Read, Grep, Glob
disable-model-invocation: true
---

你將對 img2pdf-gui 專案進行 **Coding Style** 的唯讀審查。

**嚴格禁止**：不得使用 Edit、Write、Bash 工具，不得修改任何檔案。

---

## 檢查點

- **命名慣例**：類別 PascalCase、方法與變數 camelCase、常數 UPPER_SNAKE_CASE
- **程式碼重複（DRY）**：是否有可抽取的重複邏輯
- **方法長度與複雜度**：超過 30 行或巢狀層數 > 3 的方法
- **魔術數字/字串**：未命名的字面值（應改用常數）
- **不必要的欄位或方法**：未使用的 import、private 方法、欄位

---

## 輸出格式

以 Markdown 呈現，每個問題使用以下模板：

```
### [Style] 問題標題

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
