# TASK B1 — IMPLEMENT AND INSTRUMENT THE FILE STORE (7 Marks)

## B1.1 & B1.2 — Validation & Design Comparison
- **Normal Case (`P1003`):** Returns `[P1003, 192311003, Divya, 7, 3800.0]`.
- **Damaged Line (`P1002`):** Line `"P1002,DAMAGED LINE"` splits into 2 tokens (`fields.length != 5`), so the `continue` guard skips it safely without throwing `ArrayIndexOutOfBoundsException` and allows `find("P1003")` to succeed.
- **Missing ID (`P9999`):** Returns `null` cleanly at EOF.
- **Comparison with Part A Design:** Part A specified 7 fields (`boarding_stop` and `validity_date` included), whereas the provided starter code schema uses 5 fields (`pass_id, reg_no, name, route_no, amount`), so the field-count guard was aligned to `fields.length != 5`.

## B1.3 — Buffering Measurements (1 MB, 10 MB, 50 MB)
| File Size | Unbuffered Calls | Unbuffered Time | Buffered (8 KB) Calls | Buffered Time | Speedup |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **1 MB** | 1,048,577 | 1,976.27 ms | 129 | 3.66 ms | **539.4x** |
| **10 MB** | 10,485,761 | 19,987.00 ms | 1,281 | 33.95 ms | **588.8x** |
| **50 MB** | 52,428,801 | 101,783.03 ms | 6,401 | 169.68 ms | **599.9x** |

## B1.4 — Search Growth (1,000 to 100,000 Records) & HashMap Alternative
- Worst-case `find()` scales linearly ($O(n)$): **0.697 ms** (1k) $\rightarrow$ **6.557 ms** (10k) $\rightarrow$ **33.578 ms** (50k) $\rightarrow$ **67.459 ms** (100k).
- **Alternative (`HashMap<String, String[]>`):** Reduces $q$ lookups on $n$ records from $O(q \cdot n)$ to $O(n + q)$ time ($O(1)$ average lookup) using $O(n)$ heap memory (~16 MB for 100k passes).
