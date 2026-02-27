# Convert 按鈕黑箱測試結果

## 執行摘要

```
Tests run: 63, Failures: 6, Errors: 0, Skipped: 0
```

新增 9 個測試（Section J），其中 **6 FAIL / 3 PASS**，與預期完全吻合。

---

## 測試結果明細

| # | 測試名稱 | 結果 | 備註 |
|---|---------|------|------|
| J1 | `convert_disabled_with_no_tasks` | **FAIL** | 初始無 task，按鈕應 DISABLED，實際為 ENABLED |
| J2 | `convert_enabled_when_tasks_present_no_encrypt` | PASS | 有 task 無加密，按鈕為 ENABLED ✓ |
| J3 | `convert_disabled_after_all_tasks_cleared` | **FAIL** | 清空 tasks 後按鈕應 DISABLED，實際仍 ENABLED |
| J4 | `convert_disabled_when_encrypt_on_no_password` | **FAIL** | 加密開啟但無密碼，按鈕應 DISABLED，實際 ENABLED |
| J5 | `convert_disabled_when_encrypt_on_owner_only` | **FAIL** | 加密開啟只填 ownerPassword，按鈕應 DISABLED，實際 ENABLED |
| J6 | `convert_disabled_when_encrypt_on_user_only` | **FAIL** | 加密開啟只填 userPassword，按鈕應 DISABLED，實際 ENABLED |
| J7 | `convert_enabled_when_encrypt_on_both_passwords` | PASS | 兩個密碼均填，按鈕為 ENABLED ✓ |
| J8 | `convert_disabled_when_owner_password_cleared` | **FAIL** | 清空 ownerPassword 後按鈕應 DISABLED，實際仍 ENABLED |
| J9 | `convert_enabled_after_encrypt_disabled_with_tasks` | PASS | 加密關閉後按鈕回歸 ENABLED ✓ |

---

## FAIL 原因分析

`JUIMediator` 目前的 `convertButton` 控制邏輯**僅**依賴 `setRunningState(boolean)`：
- `setRunningState(true)` → 停用 convertButton
- `setRunningState(false)` → 啟用 convertButton

缺少的邏輯：
1. **task 數量感知**：`updateTasks()` 呼叫後未依 task 清單是否為空調整按鈕狀態
2. **加密 + 密碼感知**：加密啟用時，未依兩個密碼欄位的內容是否均非空來調整按鈕狀態

---

## 期望行為（待實作）

| 條件 | Convert 應為 |
|------|-------------|
| 無 Task（初始或清空後）| DISABLED |
| 有 Task，無加密 | ENABLED |
| 有 Task，加密開啟，兩個密碼均非空 | ENABLED |
| 有 Task，加密開啟，任一密碼為空 | DISABLED |
| 加密關閉（不論之前密碼狀態）| 回到「有無 Task」規則 |

---

## 結論

6 個 FAIL 測試明確記錄了 `JUIMediator` 現有實作與期望行為之間的落差（Bug），
可作為後續實作「Convert 按鈕智慧 enable/disable」功能的驗收依據。
