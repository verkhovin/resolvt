package dev.ithurts.query

import dev.ithurts.application.exception.EntityNotFoundException
import dev.ithurts.service.repository.RepositoryInfo
import dev.ithurts.service.permission.AuthenticationFacade
import dev.ithurts.service.account.Account
import dev.ithurts.service.account.AccountRepository
import dev.ithurts.service.debt.debtevent.BindingChange
import dev.ithurts.service.debt.debtevent.DebtEvent
import dev.ithurts.service.repository.Repository
import dev.ithurts.service.repository.RepositoryRepository
import dev.ithurts.service.workspace.Workspace
import dev.ithurts.service.workspace.WorkspaceRepository
import dev.ithurts.query.model.*
import dev.ithurts.service.debt.DebtRepository
import dev.ithurts.service.debt.model.Debt
import dev.ithurts.service.debt.model.DebtStatus
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import java.time.Instant
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
class DebtQueryRepository(
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val accountRepository: AccountRepository,
    private val debtEventQueryRepository: DebtEventQueryRepository,
    private val sourceProviderService: SourceProviderService,
    private val authenticationFacade: AuthenticationFacade,
) {

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun queryDebt(debtId: String): DebtDto {
        val bindingEventsCount = debtEventQueryRepository.countByDebtId(debtId)
        return queryDebt(debtId) { debt, repository, workspace, account ->
            toDto(
                debt,
                repository,
                workspace,
                account,
                calculateDebtCost(debt, bindingEventsCount)
            )
        }
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun queryDebtDetails(debtId: String): DebtDetailsDto {
        val events = debtEventQueryRepository.findByDebtId(debtId)
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

    @PreAuthorize("hasPermission(#repositoryId, 'Repository', 'MEMBER')")
    fun queryRepositoryActiveDebts(repositoryId: String): List<DebtDto> {
        val debts = debtRepository.findByRepositoryIdAndStatusNot(repositoryId)
        val repository = repositoryRepository.findByIdOrNull(repositoryId)!!
        val workspace = workspaceRepository.findByIdOrNull(repository.workspaceId)!!
        val accounts = accountRepository.findAllById(debts.map { it.creatorAccountId })
        val eventsCount = debtEventQueryRepository.eventCountForEvents(debts.map { it.id })
        return debts.map { debt ->
            toDto(
                debt,
                repository,
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId },
                eventsCount[debt.id] ?: 0
            )
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
        val eventsCount = debtEventQueryRepository.eventCountForEvents(debts.map { it.id })
        return debts.map { debt ->
            toDto(
                debt,
                repository,
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId },
                calculateDebtCost(debt, eventsCount[debt.id] ?: 0)
            )
        }
    }

    @PreAuthorize("hasPermission(#workspaceId, 'Workspace', 'MEMBER')")
    fun queryWorkspaceDebts(workspaceId: String, resolved: Boolean): List<DebtDto> {
        val workspace = workspaceRepository.findByIdOrNull(workspaceId)
            ?: throw EntityNotFoundException("Workspace", "id", workspaceId)
        val debts = debtRepository.findByWorkspaceIdAndStatusNot(
            workspace.id,
            if (!resolved) DebtStatus.RESOLVED else DebtStatus.OPEN
        )
        val repository = repositoryRepository.findAllById(debts.map { it.repositoryId })
        val accounts = accountRepository.findAllById(debts.map { it.creatorAccountId })
        val eventsCount = debtEventQueryRepository.eventCountForEvents(debts.map { it.id })
        return debts.map { debt ->
            toDto(
                debt,
                repository.first { repo -> repo.id == debt.repositoryId },
                workspace,
                accounts.first { acc -> acc.id == debt.creatorAccountId },
                calculateDebtCost(debt, eventsCount[debt.id] ?: 0)
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

    private fun toDto(debt: Debt, repo: Repository, workspace: Workspace, reporter: Account?, cost: Int): DebtDto {
        val bindingDtos = mapBindings(debt, repo, workspace)
        return DebtDto.from(
            debt,
            bindingDtos,
            DebtRepositoryDto(repo.name),
            DebtAccountDto(reporter?.name ?: "Unknown"),
            debt.isVotedBy(authenticationFacade.account.id),
            cost
        )
    }

    private fun toDetailsDto(
        debt: Debt,
        repo: Repository,
        workspace: Workspace,
        reporter: Account?,
        events: List<DebtEvent>,
    ): DebtDetailsDto {
        val bindingDtos = mapBindings(debt, repo, workspace)
        val eventsDtos = mapBindingEvents(events, bindingDtos, repo, workspace)

        return DebtDetailsDto.from(
            debt,
            calculateDebtCost(debt, eventsDtos.size),
            bindingDtos,
            DebtRepositoryDto(repo.name),
            DebtAccountDto(reporter?.name ?: "Unknown"),
            debt.isVotedBy(authenticationFacade.account.id),
            eventsDtos
        )
    }

    private fun mapBindingEvents(
        events: List<DebtEvent>,
        bindingDtos: List<BindingDto>,
        repo: Repository,
        workspace: Workspace,
    ) = events.map { event ->
        DebtEventDto(
            event.commitHash,
            sourceProviderService.getCommitUrl(repo.name, event.commitHash, workspace.externalId),
            event.changes
                .filter(BindingChange::visible)
                .map { change ->
                    ChangeDto(
                        bindingDtos.first { it.id == change.bindingId },
                        getChangeType(change),
                        change.from,
                        change.to
                    )
                },
            event.createdAt ?: Instant.now()
        )
    }.filter { it.changes.isNotEmpty() }

    private fun getChangeType(change: BindingChange) = try {
        ChangeType.valueOf(change.type.toString())
    } catch (e: IllegalArgumentException) {
        throw Exception("Unknown change type when building debt view ${change.type}", e)
    }

    fun calculateDebtCost(debt: Debt, visibleDebtEventsCount: Int): Int {
        return debt.votes.size * 5 + visibleDebtEventsCount * 10
    }

    private fun mapBindings(
        debt: Debt,
        repo: Repository,
        workspace: Workspace,
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

    companion object {
        val log = LoggerFactory.getLogger(DebtQueryRepository::class.java)
    }
}