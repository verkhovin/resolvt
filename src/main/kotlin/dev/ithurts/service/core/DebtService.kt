package dev.ithurts.service.core

import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.controller.api.dto.TechDebtReport
import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus
import dev.ithurts.model.debt.Repository
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.repository.DebtRepository
import dev.ithurts.repository.RepositoryRepository
import dev.ithurts.security.AuthenticationFacade
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class DebtService(
    private val repositoryRepository: RepositoryRepository,
    private val debtRepository: DebtRepository,
    private val authenticationFacade: AuthenticationFacade
) {

    @PreAuthorize("hasPermission(#organisationId, 'Organisation', 'MEMBER')")
    fun createDebt(
        techDebtReport: TechDebtReport,
        organisation: Organisation,
        organisationId: Long,
        repositoryInfo: RepositoryInfo
    ): Long {
        val repository = repositoryRepository.findByNameAndOrganisation(repositoryInfo.name, organisation)
            ?: repositoryRepository.save(
                Repository(repositoryInfo.name, organisation)
            )
        return debtRepository.save(newDebt(techDebtReport, authenticationFacade.account, repository)).id!!
    }

    @PreAuthorize("hasPermission(#repository.organisation.id, 'Organisation', 'MEMBER')")
    fun getDebts(repository: Repository): List<Debt> {
        return debtRepository.findByRepositoryId(repository.id!!)
    }

    @PreAuthorize("hasPermission(#organisationId, 'Organisation', 'MEMBER')")
    fun getDebtsForOrganisation(organisationId: Long): List<Debt> {
        return debtRepository.findByOrganisationId(organisationId)
    }

    private fun newDebt(
        techDebtReport: TechDebtReport,
        account: Account,
        repository: Repository
    ) = Debt(
        techDebtReport.title,
        techDebtReport.description,
        DebtStatus.OPEN,
        techDebtReport.filePath,
        techDebtReport.startLine,
        techDebtReport.endLine,
        1,
        account,
        repository
    )

}

data class RepositoryInfo(
    val name: String,
    val organisationName: String,
    val sourceProvider: SourceProvider
)
