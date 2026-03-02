---
name: review-mvc
description: 審查 img2pdf-gui 的 MVC + Mediator 架構遵循性，輸出 Markdown 格式問題報告
allowed-tools: Read, Grep, Glob
disable-model-invocation: true
---

你將對 img2pdf-gui 專案進行 **MVC + Mediator 架構遵循性** 的唯讀審查。

**嚴格禁止**：不得使用 Edit、Write、Bash 工具，不得修改任何檔案。

---

## 評估態度

MVC 違規視為高嚴重度，必須詳細描述。不得以「輕微可接受」寬鬆帶過。

---

## 檢查點

- View（`View.java`、`JUIMediator.java`）是否直接呼叫 Model 方法？（違反 MVC）
- Controller 是否正確橋接 Model 與 Mediator，未跳過任何一方？
- `JUIMediator` 的方法是否直接觸發業務邏輯（例如解析檔案、計算格式），而非只更新 UI 狀態？
- `UIState` 是否被 Model 或 Controller 直接讀取（應僅由 Mediator 管理）？
- `Controller` 是否持有 `View` 具體類別的參照（應只依賴 `UIMediator` 介面）？
- `MediatorListener` 介面是否保持最小且職責清晰？

---

## 輸出格式

以 Markdown 呈現，每個問題使用以下模板：

```
### [MVC] 問題標題

- **位置**：`檔案路徑:行號`
- **問題**：具體描述問題是什麼
- **建議方向**：改善方向（不提供修改後的程式碼）
- **嚴重度**：高 / 中 / 低
- **重構成本評估**：需修改 [檔案列表]，估計影響 N 行，主要風險為 [xxx]
```

**若發現違規，必須填寫「重構成本評估」欄位。**

節末提供**小結**，列出此面向的整體評估（良好 / 待改善 / 需重構）。

---

## 絕對限制

- **只讀不改**：本技能為純審查，禁止提出具體程式碼修改。
- **不猜測程式行為**：有疑問時以 Grep 搜尋確認，不憑印象判斷。
