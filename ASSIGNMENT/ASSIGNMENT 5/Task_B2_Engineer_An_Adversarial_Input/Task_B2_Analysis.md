# TASK B2 — ENGINEER AN ADVERSARIAL INPUT (5 Marks)

## Adversarial Inputs & Observed Behaviour
1. **Tautology Bypass:** `username = "admin' OR '1'='1' -- "` $\rightarrow$ `unsafeLogin()` returns `true` (bypasses password check); `safeLogin()` returns `false`.
2. **UNION Row Injection:** `username = "' UNION SELECT 1,'attacker','admin' -- "` $\rightarrow$ `unsafeLogin()` returns `true`; `safeLogin()` returns `false`.
3. **Stacked Destructive Query:** `username = "'; DROP TABLE users; --"` $\rightarrow$ `unsafeLogin()` exposes query structure; `safeLogin()` binds it harmlessly as a literal string.

## Alternative Concept & Complexity Analysis (Quote Escaping / Regex vs. `PreparedStatement`)
- Escaping quotes or regex filtering is weaker because unquoted numeric contexts remain vulnerable, multibyte character encodings can bypass naive escaping, and regex validation still requires $O(L)$ string scanning plus full SQL parsing/compilation on every execution. `PreparedStatement` pre-compiles the SQL AST once and binds typed parameters directly.
