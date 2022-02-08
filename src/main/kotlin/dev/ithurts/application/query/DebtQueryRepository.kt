package dev.ithurts.application.query

import dev.ithurts.application.dto.debt.*
import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.application.service.RepositoryInfo
import dev.ithurts.application.service.SourceProviderService
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.domain.bindingevent.BindingEvent
import dev.ithurts.domain.bindingevent.BindingEventRepository
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
    private val bindingEventRepository: BindingEventRepository,
    private val sourceProviderService: SourceProviderService,
    private val authenticationFacade: AuthenticationFacade,
) {

    fun queryDebt(debtId: String): DebtDto {
        return queryDebt(debtId) { debt, repository, workspace, account ->
            toDto(
                debt,
                repository,
                workspace,
                account
            )
        }
    }

    fun queryDebtDetails(debtId: String): DebtDetailsDto {
        val events = bindingEventRepository.findByDebtId(debtId)
        return queryDebt(debtId) { debt, repository, workspace, account ->
            toDetailsDto(
                debt,
                repository,
                workspace,
                account,
                events
            )
        }
    }

    private fun <T> queryDebt(debtId: String, mapper: (Debt, Repository, Workspace, Account?) -> T): T {
        val debt = debtRepository.findByIdOrNull(debtId)
            ?: throw EntityNotFoundException("Debt", "id", debtId)
        val workspace = workspaceRepository.findByIdOrNull(debt.workspaceId)!!
        val repository = repositoryRepository.findByIdOrNull(debt.repositoryId)!!
        val account = accountRepository.findByIdOrNull(debt.creatorAccountId)

        return mapper(debt, repository, workspace, account)
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
                repository.first { repo -> repo.id == debt.repositoryId },
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId })
        }
    }

    private fun toDto(debt: Debt, repo: Repository, workspace: Workspace, reporter: Account?): DebtDto {
        return DebtDto.from(
            debt,
            SourceLink(
                sourceProviderService.getSourceUrl(
                    debt.bindings[0],
                    repo.name,
                    repo.mainBranch,
                    workspace.externalId
                ),
                getFileName(debt.bindings[0].filePath)
            ),
            DebtRepositoryDto(repo.name),
            DebtAccountDto(reporter?.name ?: "Unknown"),
            debt.accountVoted(authenticationFacade.account.id)
        )
    }

    private fun toDetailsDto(
        debt: Debt,
        repo: Repository,
        workspace: Workspace,
        reporter: Account?,
        events: List<BindingEvent>
    ): DebtDetailsDto {
        val bindingDtos = mapBindings(debt, repo, workspace)
        val eventsDtos = mapBindingEvents(events, bindingDtos, repo, workspace)

        return DebtDetailsDto.from(
            debt,
            SourceLink(
                sourceProviderService.getSourceUrl(
                    debt.bindings[0],
                    repo.name,
                    repo.mainBranch,
                    workspace.externalId
                ),
                getFileName(debt.bindings[0].filePath)
            ),
            bindingDtos,
            DebtRepositoryDto(repo.name),
            DebtAccountDto(reporter?.name ?: "Unknown"),
            debt.accountVoted(authenticationFacade.account.id),
            eventsDtos
        )
    }

    private fun mapBindingEvents(
        events: List<BindingEvent>,
        bindingDtos: List<BindingDto>,
        repo: Repository,
        workspace: Workspace
    ) = events.groupBy { it.commitHash }
        .toList()
        .sortedWith(Comparator.comparing { it.second[0].createdAt })
        .map { commit ->
            CommitEventsDto(
                commit.first,
                sourceProviderService.getCommitUrl(repo.name, commit.first, workspace.externalId),
                commit.second.map { event ->
                    BindingEventDto(
                        bindingDtos.first { it.id == event.bindingId },
                        event.changes.map { ChangeDto(it.type, it.from, it.to) },
                        event.createdAt
                    )
                }
            )
        }

    private fun mapBindings(
        debt: Debt,
        repo: Repository,
        workspace: Workspace
    ) = debt.bindings.map { binding ->
        BindingDto.from(
            binding,
            SourceLink(
                sourceProviderService.getSourceUrl(
                    binding,
                    repo.name,
                    repo.mainBranch,
                    workspace.externalId
                ),
                getFileName(binding.filePath)
            )
        )
    }


    private fun getFileName(path: String) = path.substringAfterLast("/")
}