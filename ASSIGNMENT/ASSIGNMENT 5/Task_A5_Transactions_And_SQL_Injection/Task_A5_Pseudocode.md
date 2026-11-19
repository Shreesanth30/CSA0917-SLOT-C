# TASK A5 — TRANSACTIONS & SQL INJECTION DESIGN (4 Marks)

## A5.1 — Atomic Transaction Pseudocode
```text
ALGORITHM TransferPassFeeAtomic(con, sourceRegNo, targetRegNo, amount)
INPUT: con (Connection), sourceRegNo (String), targetRegNo (String), amount (Double)
BEGIN
    DECLARE previousAutoCommit <- con.getAutoCommit()
    con.setAutoCommit(FALSE)
    TRY
        PREPARE psDebit <- con.prepareStatement("UPDATE student_wallet SET balance = balance - ? WHERE reg_no = ?")
        BIND psDebit[1] <- amount; BIND psDebit[2] <- sourceRegNo
        EXECUTE_UPDATE psDebit

        PREPARE psCredit <- con.prepareStatement("UPDATE student_wallet SET balance = balance + ? WHERE reg_no = ?")
        BIND psCredit[1] <- amount; BIND psCredit[2] <- targetRegNo
        EXECUTE_UPDATE psCredit

        con.commit()
    CATCH exception
        con.rollback()
        RETHROW exception
    FINALLY
        con.setAutoCommit(previousAutoCommit)
    END TRY
END
```

## A5.2 — Safe Login Check Pseudocode (`PreparedStatement`)
```text
ALGORITHM AuthenticateUserSafe(con, inputUsername, inputPassword)
INPUT: con (Connection), inputUsername (String), inputPassword (String)
OUTPUT: BOOLEAN (TRUE if authenticated, FALSE otherwise)
BEGIN
    PREPARE ps <- con.prepareStatement("SELECT id, role FROM users WHERE username = ? AND password = ?")
    BIND ps[1] <- inputUsername
    BIND ps[2] <- inputPassword
    EXECUTE rs <- ps.executeQuery()
    RETURN rs.next()
END
```
