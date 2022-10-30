package dev.ithurts.application.events

import dev.ithurts.application.model.end
import dev.ithurts.application.model.start
import dev.ithurts.application.service.internal.diff.advancedbinding.AdvancedBindingService
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtReportedEvent
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.repository.Repository
import dev.ithurts.domain.repository.RepositoryRepository
import org.springframework.context.ApplicationListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class DebtReportedEventListener(
    private val advancedBindingService: AdvancedBindingService,
    private val repositoryRepository: RepositoryRepository,
    private val debtRepository: DebtRepository,
) : ApplicationListener<DebtReportedEvent> {
    override fun onApplicationEvent(event: DebtReportedEvent) {
        val debt = event.debt
        val repository = repositoryRepository.findByIdOrNull(debt.repositoryId)!!
        val locatedBindings = debt.bindings.map { binding ->
            if (binding.isAdvanced()) {
                locateAdvancedBinding(binding, debt, repository)
            } else binding
        }
        debtRepository.save(
            debt.copy(
                bindings = locatedBindings
            )
        )
    }

    private fun locateAdvancedBinding(
        binding: Binding,
        debt: Debt,
        repository: Repository,
    ): Binding {
        val advancedBinding = binding.advancedBinding!!
        val location = advancedBindingService.lookupBindingLocation(
            debt, advancedBinding, binding.filePath, repository.mainBranch
        ) ?: (binding.startLine to binding.endLine)
        return binding.copy(
            startLine = location.start,
            endLine = location.end
        )
    }
}