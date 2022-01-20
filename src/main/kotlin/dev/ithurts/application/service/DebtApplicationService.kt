package dev.ithurts.application.service

import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.exception.DebtReportFailedException
import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.controller.web.page.DebtEditForm
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.repository.RepositoryService
import dev.ithurts.exception.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class DebtApplicationService(
    private val repositoryService: RepositoryService,
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val authenticationFacade: AuthenticationFacade,
) {
    @PreAuthorize("hasPermission(#repositoryInfo, 'Repository', 'MEMBER')")
    fun createDebt(techDebtReport: TechDebtReport, repositoryInfo: RepositoryInfo): Long {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.workspaceExternalId
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.workspaceExternalId}")

        val repository = repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.identity)
            ?: repositoryService.acknowledgeExternalRepositoryByWorkspace(workspace, repositoryInfo.name).let {
                repositoryRepository.save(it)
            }

        val debt = repository.reportDebt(techDebtReport, authenticationFacade.account.identity)
        return debtRepository.save(debt).identity
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun edit(debtId: Long, changes: DebtEditForm) {
        val (start, end) = changes.linesSpec.split(':', limit = 2).map { it.toInt() }
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId.toString())
        debt.update(
            changes.title,
            changes.description,
            changes.status!!,
            changes.filePath,
            start,
            end
        )
        debtRepository.save(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun vote(debtId: Long) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId.toString())
        debt.vote(authenticationFacade.account.identity)
        debtRepository.save(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun downVote(debtId: Long) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId.toString())
        debt.downVote(authenticationFacade.account.identity)
        debtRepository.save(debt)
    }

}