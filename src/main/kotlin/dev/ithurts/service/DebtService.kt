package dev.ithurts.service

import dev.ithurts.exception.DebtReportFailedException
import dev.ithurts.model.Account
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.api.TechDebtReport
import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus
import dev.ithurts.model.debt.Repository
import dev.ithurts.model.organisation.Organisation
import dev.ithurts.repository.DebtRepository
import dev.ithurts.repository.OrganisationRepository
import dev.ithurts.repository.RepositoryRepository
import dev.ithurts.security.AuthenticationFacade
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class DebtService(
    private val organisationRepository: OrganisationRepository,
    private val repositoryRepository: RepositoryRepository,
    private val debtRepository: DebtRepository,
    private val authenticationFacade: AuthenticationFacade
) {
    fun createDebt(techDebtReport: TechDebtReport): Long {
        val repositoryInfo: RepositoryInfo = parseRemoteUrl(techDebtReport.remoteUrl)
        val organisation = organisationRepository.getBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.organisationName
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.organisationName}")
        return createDebt(techDebtReport, organisation, repositoryInfo)
    }

    @PreAuthorize("hasPermission(#organisation.id, 'Organisation', 'MEMBER')")
    fun createDebt(
        techDebtReport: TechDebtReport,
        organisation: Organisation,
        repositoryInfo: RepositoryInfo
    ): Long {
        val repository = repositoryRepository.findByNameAndOrganisation(repositoryInfo.name, organisation)
            ?: repositoryRepository.save(
                Repository(repositoryInfo.name, organisation)
            )
        return debtRepository.save(newDebt(techDebtReport, authenticationFacade.account, repository)).id!!
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


    private fun parseRemoteUrl(remoteUrl: String): RepositoryInfo {
        val matchResult =
            REMOTE_URL_REGEX.matchEntire(remoteUrl) ?: throw DebtReportFailedException("Failed to parse remote url")
        val name = matchResult.groups["name"]?.value
            ?: throw DebtReportFailedException("Failed to parse repo name out of remote url")
        val organisationName = matchResult.groups["organisation"]?.value
            ?: throw DebtReportFailedException("Failed to parse organisation name out of remote url")
        return RepositoryInfo(name, organisationName, SourceProvider.BITBUCKET)
    }

    companion object {
        private val REMOTE_URL_REGEX =
            "(?<host>(git@|https://)([\\w.@]+)([/:]))(?<organisation>[\\w,\\-_]+)/(?<name>[\\w,\\-_]+)(.git)?((/)?)".toRegex()
    }
}

data class RepositoryInfo(
    val name: String,
    val organisationName: String,
    val sourceProvider: SourceProvider
)
