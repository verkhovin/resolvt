package dev.ithurts.application.query

import dev.ithurts.application.dto.AccountDTO
import dev.ithurts.application.dto.DebtDTO
import dev.ithurts.application.dto.RepositoryDTO
import dev.ithurts.application.dto.SourceLink
import dev.ithurts.application.service.RepositoryInfoService
import dev.ithurts.application.service.SourceProviderService
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.exception.DebtReportFailedException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
//TODO do db joins instead of using domain repositorties
class DebtQueryService(
    private val repositoryRepository: RepositoryRepository,
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryInfoService: RepositoryInfoService,
    private val accountRepository: AccountRepository,
    private val sourceProviderService: SourceProviderService,
) {
    fun getActiveDebts(repositoryId: Long): List<DebtDTO> {
        return emptyList()
//        return debtRepository.findByRepositoryIdAndStatusNot(repositoryId)
    }

    fun getActiveDebtsForWorkspace(workspaceId: Long): List<Debt> {
        return debtRepository.findByWorkspaceIdAndStatusNot(workspaceId)
    }

    fun getActiveDebts(repositoryRemoteUrl: String): List<DebtDTO> {
        val repositoryInfo = repositoryInfoService.parseRemoteUrl(repositoryRemoteUrl)
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.organisationName
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.organisationName}")
        val repository = repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.id)
            ?: throw DebtReportFailedException("No repository found for ${repositoryInfo.name}")

        val debts = debtRepository.findByRepositoryIdAndStatusNot(repository.id)

        val accounts: Map<Long, Account> = accountRepository.findAllById(debts.map { it.creatorAccountId }).associateBy { it.id }

        return debts.map { debt ->
            DebtDTO.from(
                debt,
                SourceLink(
                    sourceProviderService.getSourceUrl(
                        debt,
                        repository.name,
                        repository.mainBranch,
                        workspace.externalId
                    ),
                    getFileName(debt.filePath)
                ),
                RepositoryDTO(repository.name),
                AccountDTO(accounts[debt.creatorAccountId]?.name ?: "Unknown")
            )
        }
    }

    private fun getFileName(path: String) = path.substringAfterLast("/")


}