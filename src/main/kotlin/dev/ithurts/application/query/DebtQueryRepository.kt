package dev.ithurts.application.query

import dev.ithurts.application.exception.EntityNotFoundException
import dev.ithurts.application.model.RepositoryInfo
import dev.ithurts.application.model.debt.*
import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.domain.CostCalculationService
import dev.ithurts.domain.account.Account
import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.debt.DebtStatus
import dev.ithurts.domain.debtevent.BindingChange
import dev.ithurts.domain.debtevent.DebtEvent
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.workspace.Workspace
import dev.ithurts.domain.workspace.WorkspaceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository as SpringRepository

@SpringRepository
class DebtQueryRepository(
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val accountRepository: AccountRepository,
    private val debtEventQueryRepository: DebtEventQueryRepository,
    private val sourceProviderService: SourceProviderService,
    private val costCalculationService: CostCalculationService,
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
                costCalculationService.calculateCost(debt, bindingEventsCount)
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
                costCalculationService.calculateCost(debt, eventsCount[debt.id] ?: 0)
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
                costCalculationService.calculateCost(debt, eventsCount[debt.id] ?: 0)
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
            costCalculationService.calculateCost(debt, events.size),
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
                .filter { change -> change.type != dev.ithurts.domain.debt.ChangeType.CODE_MOVED }
                .map { change ->
                    ChangeDto(
                        bindingDtos.first { it.id == change.bindingId },
                        getChangeType(change),
                        change.from,
                        change.to
                    )
                },
            event.createdAt
        )
    }

    private fun getChangeType(change: BindingChange) = try {
        ChangeType.valueOf(change.type.toString())
    } catch (e: IllegalArgumentException) {
        log.error("Unknown change type when building debt view ${change.type}", e)
        ChangeType.GENERIC
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