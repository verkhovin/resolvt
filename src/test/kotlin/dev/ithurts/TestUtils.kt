package dev.ithurts

import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus

fun debtMock(startLine: Int, endLine: Int): Debt {
    return Debt(
        "",
        "",
        DebtStatus.OPEN,
        "0",
        "1",
        "1",
        mutableListOf(Binding("src/main/java/ru/verkhovin/poker/model/Room.java", startLine, endLine, null)),

    )
}