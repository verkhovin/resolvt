package dev.ithurts

import dev.ithurts.domain.account.Account
import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.workspace.Workspace


fun debtMock(startLine: Int, endLine: Int): Debt {
//    val account = Account("", "", SourceProvider.BITBUCKET, "1")
//    val repository = Repository("", "main", Workspace("", SourceProvider.BITBUCKET, "", "", ""))
//    val repository = Repository("", "main", 1)
    return Debt(
        "",
        "",
        DebtStatus.OPEN,
        0,
        1,
        mutableListOf(Binding("src/main/java/ru/verkhovin/poker/model/Room.java", startLine, endLine, null)),
        null

    )
}