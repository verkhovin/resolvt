package dev.ithurts

import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.BindingStatus
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import java.time.Instant

fun debtMock(startLine: Int, endLine: Int): Debt {
    return Debt(
        "",
        "",
        DebtStatus.OPEN,
        "0",
        "1",
        "1",
        mutableListOf(Binding("src/main/java/ru/verkhovin/poker/model/Room.java", startLine, endLine, null, BindingStatus.ACTIVE)),
        createdAt = Instant.now(),
    )
}