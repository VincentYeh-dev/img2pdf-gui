---
name: review-performance
description: 審查 img2pdf-gui 主程式的時間與空間複雜度，輸出 Markdown 格式問題報告
allowed-tools: Read, Grep, Glob
disable-model-invocation: true
---

你將對 img2pdf-gui 專案進行 **時間與空間複雜度** 的唯讀效能審查，特別關注迴圈、N 很大的結構、無界快取與記憶體配置。

**嚴格禁止**：不得使用 Edit、Write、Bash 工具，不得修改任何檔案。

---

## 檢查點

### 1. 迴圈與演算法複雜度

- **巢狀迴圈（O(n²) 風險）**：找出兩層以上的迴圈，評估最壞情況複雜度
- **排序複雜度**：`Collections.sort`、`Stream.sorted`、`Arrays.sort` 的 O(n log n) 是否在大 n 下被多次呼叫
- **Matcher 重複掃描**：`Pattern.matcher().find()` 是否在每次比較或格式化時重新執行，而非預先彙整結果
- **偏好迴圈而非遞迴**：若發現遞迴呼叫，標示 Stack 深度風險（StackOverflowError），建議改寫為迭代

### 2. 字串與集合操作

- **String.replace / replaceAll in loop**：每次替換均產生新字串物件；迴圈內多次替換應改用 `StringBuilder`
- **Pattern.compile() 重複呼叫**：是否在方法內部（每次呼叫）重新編譯 Regex，而非提升為 static final 常數
- **HashMap / ArrayList 初始容量**：集合在可預知大小時是否指定初始容量，避免多次 rehash / array copy
- **適當資料結構**：`ArrayList` vs `LinkedList` 選用是否符合存取模式（隨機存取 vs 頻繁插入刪除）

### 3. 記憶體與 N 很大的結構

- **無界快取（thumbnailCache）**：`HashMap` 作為快取卻無大小上限，長時間執行可能持續成長導致 OOM
- **Files.walk + sorted(reverseOrder)**：`sorted()` 需緩衝所有路徑才能排序，目錄樹很深時記憶體用量線性增長
- **大圖載入無 size limit**：`ImageIO.read()` 不限制解析度，超大圖片可能一次性佔用數百 MB heap
- **byte[] / BufferedImage 重複配置**：迴圈內每次迭代是否重新配置大型陣列或圖片物件，而非重用

---

## 輸出格式

以 Markdown 呈現，每個問題使用以下模板：

```
### [效能] 問題標題

- **位置**：`檔案路徑:行號`
- **問題**：具體描述問題是什麼
- **Big-O**：目前複雜度（時間 / 空間）
- **建議方向**：改善方向（不提供修改後的程式碼）
- **嚴重度**：高 / 中 / 低
```

節末提供**小結**，列出此面向的整體評估（良好 / 待改善 / 需修正）。

---

## 絕對限制

- **只讀不改**：本技能為純審查，禁止提出具體程式碼修改。
- **不猜測程式行為**：有疑問時以 Grep 搜尋確認，不憑印象判斷。