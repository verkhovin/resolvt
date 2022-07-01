package dev.ithurts.domain.repository

import dev.ithurts.application.model.TechDebtReport
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "repositories")
data class Repository(
    val name: String,
    val mainBranch: String,
    val workspaceId: String,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!

    fun rename(newName: String): Repository = this.copy(name = newName)

    fun reportDebt(techDebtReport: TechDebtReport, reportedByAccountId: String, instant: Instant): Debt {
        return Debt(
            techDebtReport.title,
            techDebtReport.description,
            DebtStatus.OPEN,
            reportedByAccountId,
            this.id,
            workspaceId,
            techDebtReport.bindings.map { it.toDomain() }.toMutableList(),
            instant
        ).vote(reportedByAccountId)
    }
}