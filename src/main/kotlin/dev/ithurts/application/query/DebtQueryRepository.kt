package dev.ithurts.application.query

import dev.ithurts.application.dto.debt.DebtAccountDto
import dev.ithurts.application.dto.debt.DebtDto
import dev.ithurts.application.dto.debt.DebtRepositoryDto
import dev.ithurts.application.dto.debt.SourceLink
import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.application.service.SourceProviderService
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.exception.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
class DebtQueryRepository(
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val accountRepository: AccountRepository,
    private val sourceProviderService: SourceProviderService,
    private val authenticationFacade: AuthenticationFacade,
) {

    fun queryDebt(debtId: String): DebtDto {
        val debt = debtRepository.findByIdOrNull(debtId)
            ?: throw EntityNotFoundException("Debt", "id", debtId)
        val workspace = workspaceRepository.findByIdOrNull(debt.workspaceId)!!
        val repository = repositoryRepository.findByIdOrNull(debt.repositoryId)!!
        val account = accountRepository.findByIdOrNull(debt.creatorAccountId)
        return toDto(debt, repository, workspace, account)
    }


    @PreAuthorize("hasPermission(#repositoryId, 'Repository', 'MEMBER')")
    fun queryRepositoryActiveDebts(repositoryId: String): List<DebtDto> {
        val debts = debtRepository.findByRepositoryIdAndStatusNot(repositoryId)
        val repository = repositoryRepository.findByIdOrNull(repositoryId)!!
        val workspace = workspaceRepository.findByIdOrNull(repository.workspaceId)!!
        val accounts = accountRepository.findAllById(debts.map { it.creatorAccountId })
        return debts.map { debt ->
            toDto(
                debt,
                repository,
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId })
        }
    }

    @PreAuthorize("hasPermission(#repositoryInfo, 'Repository', 'MEMBER')")
    fun queryRepositoryActiveDebts(repositoryInfo: RepositoryInfo): List<DebtDto> {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.workspaceExternalId
        )!!
        val repository = repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.id)!!
        val debts = debtRepository.findByRepositoryIdAndStatusNot(repository.id)
        val accounts = accountRepository.findAllById(debts.map { it.creatorAccountId })
        return debts.map { debt ->
            toDto(
                debt,
                repository,
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId })
        }
    }

    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    fun queryWorkspaceDebts(workspaceId: String): List<DebtDto> {
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)
            ?: throw EntityNotFoundException("Workspace", "id", workspaceId)
        val debts = debtRepository.findByWorkspaceIdAndStatusNot(workspace.id)
        val repository = repositoryRepository.findAllById(debts.map { it.repositoryId })
        val accounts = accountRepository.findAllById(debts.map { it.creatorAccountId })
        return debts.map { debt ->
            toDto(
                debt,
                repository.first{ repo -> repo.id == debt.repositoryId },
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId })
        }
    }

    private fun toDto(debt: Debt, repo: Repository, workspace: Workspace, reporter: Account?): DebtDto {
        return DebtDto.from(
            debt,
            SourceLink(
                sourceProviderService.getSourceUrl(
                    debt,
                    repo.name,
                    repo.mainBranch,
                    workspace.externalId
                ),
                getFileName(debt.filePath)
            ),
            DebtRepositoryDto(repo.name),
            DebtAccountDto(reporter?.name ?: "Unknown"),
            debt.accountVoted(authenticationFacade.account.id)
        )
    }

    private fun getFileName(path: String) = path.substringAfterLast("/")
}