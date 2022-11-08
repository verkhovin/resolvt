package dev.ithurts.service.debt

import dev.ithurts.application.model.end
import dev.ithurts.application.model.start
import dev.ithurts.service.debt.diff.advancedbinding.AdvancedBindingService
import dev.ithurts.service.debt.model.Binding
import dev.ithurts.service.debt.model.Debt
import dev.ithurts.service.repository.Repository
import dev.ithurts.service.repository.RepositoryRepository
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