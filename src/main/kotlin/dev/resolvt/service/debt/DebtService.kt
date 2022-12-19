package dev.resolvt.service.debt

import dev.resolvt.api.web.page.AdvancedBindingEditForm
import dev.resolvt.api.web.page.BindingEditForm
import dev.resolvt.api.web.page.DebtEditForm
import dev.resolvt.application.exception.EntityNotFoundException
import dev.resolvt.application.model.end
import dev.resolvt.application.model.start
import dev.resolvt.service.debt.model.*
import dev.resolvt.service.permission.AuthenticationFacade
import dev.resolvt.service.repository.Repository
import dev.resolvt.service.repository.RepositoryInfo
import dev.resolvt.service.repository.RepositoryService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class DebtService(
    private val repositoryService: RepositoryService,
    private val debtRepository: DebtRepository,
    private val authenticationFacade: AuthenticationFacade,
    private val archiveService: DebtArchiveService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val clock: Clock,
) {
    @PreAuthorize("hasPermission(#repositoryInfo, 'Repository', 'MEMBER')")
    fun createDebt(techDebtReport: DebtReport, repositoryInfo: RepositoryInfo): String {
        val repository = repositoryService.ensureRepository(repositoryInfo)
        val reporterAccountId = authenticationFacade.account.id
        val debt = Debt(
            techDebtReport.title,
            techDebtReport.description,
            DebtStatus.OPEN,
            reporterAccountId,
            repository.id,
            repository.workspaceId,
            techDebtReport.bindings.map { it.toDomain() }.toMutableList(),
            clock.instant(),
            votes = listOf(DebtVote(reporterAccountId))
        )
        val savedDebt = debtRepository.save(debt)
        applicationEventPublisher.publishEvent(DebtReportedEvent(savedDebt, this)) //TODO async
        return savedDebt.id
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun update(debtId: String, debtDto: DebtReport) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debtRepository.save(
            debt.update(debtDto.title, debtDto.description, DebtStatus.OPEN, clock.instant())
                .rebind(debtDto.bindings.map { it.toDomain() })
        )
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun update(debtId: String, changes: DebtEditForm) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debtRepository.save(debt.update(changes.title, changes.description, changes.status, clock.instant()))
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun vote(debtId: String) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debtRepository.save(debt.vote(authenticationFacade.account.id))
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun downVote(debtId: String) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        debtRepository.save(debt.downVote(authenticationFacade.account.id))
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
        debtRepository.save(debt.updateBinding(bindingId, form.path, start, end))
    }

    @PreAuthorize("hasPermission(#debtId, 'Debt', 'MEMBER')")
    fun editAdvancedBinding(debtId: String, bindingId: String, form: AdvancedBindingEditForm) {
        val debt = debtRepository.findByIdOrNull(debtId) ?: throw EntityNotFoundException("Debt", "id", debtId)
        val params = form.params?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        debtRepository.save(
            debt.updateAdvancedBinding(
                bindingId,
                form.path,
                if (form.parent.isNullOrBlank()) null else form.parent,
                form.name ?: "",
                params
            )
        )
    }

}