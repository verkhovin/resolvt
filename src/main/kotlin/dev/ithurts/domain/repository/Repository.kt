package dev.ithurts.domain.repository

import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "repositories")
data class Repository(
    var name: String,
    var mainBranch: String,
    val workspaceId: String,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!

    fun rename(newName: String) {
        name = newName
    }

    fun reportDebt(techDebtReport: TechDebtReport, reportedByAccountId: String): Debt {
        return Debt(
            techDebtReport.title,
            techDebtReport.description,
            DebtStatus.OPEN,
            reportedByAccountId,
            this.id,
            workspaceId,
            techDebtReport.bindings.map { it.toDomain() }.toMutableList()
        ).also { it.vote(reportedByAccountId) }
    }
}