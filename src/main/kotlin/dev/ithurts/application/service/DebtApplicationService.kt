package dev.ithurts.application.service

import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.controller.web.page.AdvancedBindingEditForm
import dev.ithurts.controller.web.page.BindingEditForm
import dev.ithurts.controller.web.page.DebtEditForm
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.repository.RepositoryService
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.exception.DebtReportFailedException
import dev.ithurts.exception.EntityNotFoundException
import dev.ithurts.external.ArchiveService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class DebtApplicationService(
    private val repositoryService: RepositoryService,
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val archiveService: ArchiveService,
    private val clock: Clock,
) {
    @PreAuthorize("hasPermission(#repositoryInfo, 'Repository', 'MEMBER')")
    fun createDebt(techDebtReport: TechDebtReport, repositoryInfo: RepositoryInfo): String {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider, repositoryInfo.workspaceExternalId
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.workspaceExternalId}")

        val repository = repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.id)
            ?: repositoryService.acknowledgeExternalRepositoryByWorkspace(workspace, repositoryInfo.name).let {
                repositoryRepository.save(it)
            }

        val debt = repository.reportDebt(techDebtReport, authenticationFacade.account.id, clock.instant())
        return debtRepository.save(debt).id
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun edit(debtId: String, changes: DebtEditForm) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debt.update(
            changes.title, changes.description, changes.status, clock.instant()
        )
        debtRepository.save(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun vote(debtId: String) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debt.vote(authenticationFacade.account.id)
        debtRepository.save(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun downVote(debtId: String) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debt.downVote(authenticationFacade.account.id)
        debtRepository.save(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun deleteDebt(debtId: String) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        archiveService.archiveDebt(debt)
        debtRepository.delete(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun editBinding(debtId: String, bindingId: String, form: BindingEditForm) {
        val (start, end) = form.linespec!!.split(':', limit = 2).map { it.toInt() }
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debt.bindings.firstOrNull { it.id == bindingId }?.update(form.path, false, start, end)
        debtRepository.save(debt)
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun editAdvancedBinding(debtId: String, bindingId: String, form: AdvancedBindingEditForm) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debt.bindings.first { it.id == bindingId }.let { binding ->
            binding.updateAdvancedManually(
                form.path,
                form.parent,
                form.name ?: "",
                form.params?.split(',')?.map { it.trim() } ?: emptyList()
            )
        }
        debtRepository.save(debt)
    }

}