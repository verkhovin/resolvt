package dev.ithurts

import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus
import dev.ithurts.model.debt.Repository
import dev.ithurts.model.organisation.Organisation


fun debtMock(startLine: Int, endLine: Int): Debt {
    val account = Account("", "", SourceProvider.BITBUCKET, "1")
    val repository = Repository("", "main", Organisation("", SourceProvider.BITBUCKET, "", "", ""))
    return Debt(
        "",
        "",
        DebtStatus.OPEN,
        "src/main/java/ru/verkhovin/poker/model/Room.java",
        startLine, endLine, 0, account, repository, 0
    )
}