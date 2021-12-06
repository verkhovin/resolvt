package dev.ithurts.service

import dev.ithurts.exception.DebtReportFailedException
import dev.ithurts.exception.EntityNotFoundException
import dev.ithurts.model.SourceProvider
import dev.ithurts.model.api.DebtDTO
import dev.ithurts.model.api.TechDebtReport
import dev.ithurts.model.debt.Debt
import dev.ithurts.repository.OrganisationRepository
import dev.ithurts.repository.RepositoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DebtApiService(
    private val debtService: DebtService,
    private val organisationRepository: OrganisationRepository,
    private val repositoryRepository: RepositoryRepository,
) {
    fun createDebt(techDebtReport: TechDebtReport): Long {
        val repositoryInfo: RepositoryInfo = parseRemoteUrl(techDebtReport.remoteUrl)
        val organisation = organisationRepository.getBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.organisationName
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.organisationName}")
        return debtService.createDebt(techDebtReport, organisation, organisation.id!!, repositoryInfo)
    }

    fun getDebts(repositoryId: Long): List<Debt> {
        val repository = repositoryRepository.findByIdOrNull(repositoryId)
            ?: throw EntityNotFoundException("repository", "id", repositoryId.toString())
        return debtService.getDebts(repository)
    }

    fun getDebts(repositoryRemoteUrl: String): List<DebtDTO> {
        val repositoryInfo = parseRemoteUrl(repositoryRemoteUrl)
        val organisation = organisationRepository.getBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.organisationName
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.organisationName}")
        val repository = repositoryRepository.findByNameAndOrganisation(repositoryInfo.name, organisation)
            ?: throw DebtReportFailedException("No repository found for ${repositoryInfo.name}")
        return debtService.getDebts(repository).map { DebtDTO.from(it) }
    }

    fun getDebtsForOrganisation(organisationId: Long): List<Debt> {
        return debtService.getDebtsForOrganisation(organisationId)
    }

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