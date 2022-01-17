package dev.ithurts.domain.repository

import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.domain.DomainEntity
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtStatus
import javax.persistence.*

@Entity
class Repository(
    var name: String,
    var mainBranch: String,
    val workspaceId: Long,
): DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null

    fun rename(newName: String) {
        name = newName
    }

    fun reportDebt(techDebtReport: TechDebtReport, reportedByAccountId: Long): Debt {
        return Debt(
            techDebtReport.title,
            techDebtReport.description,
            DebtStatus.OPEN,
            techDebtReport.filePath,
            techDebtReport.startLine,
            techDebtReport.endLine,
            1,
            reportedByAccountId,
            this.identity,
        )
    }
}