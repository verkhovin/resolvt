package dev.ithurts.application.service.diff

import dev.ithurts.application.dto.PushInfo
import dev.ithurts.application.service.diff.binding.BindingAdjustmentService
import dev.ithurts.application.service.diff.binding.advanced.AdvancedBindingAdjustmentService
import dev.ithurts.application.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.Debt
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class DebtDiffApplicator(
    private val bindingAdjustmentService: BindingAdjustmentService,
    private val advancedBindingAdjustmentService: AdvancedBindingAdjustmentService,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService
) {
    fun applyDiffs(debt: Debt, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo) {
        debt.bindings.forEach { binding ->
            if (binding.isAdvanced()) {
                handleAdvancedBinding(binding, diffsByFile, pushInfo)
            } else {
                handleBasicBinding(binding, diffsByFile)
            }
        }
    }

    // TODO probably, following two methods should be classes implemented the same interface

    private fun handleAdvancedBinding(binding: Binding, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo) {
        val boundFileDiffs: List<Diff> = diffsByFile[binding.filePath] ?: return
        val filePath = trimDiffFilepath(boundFileDiffs.last().toFileName)

        //TODO probably this class shouldn't know that file should be fetched from source provider. delegate this responsibility to another class?
        val fileContent = sourceProviderCommunicationService.getFile(
            pushInfo.workspaceExternalId,
            pushInfo.repositoryExternalId,
            filePath,
            pushInfo.commitHash
        )
        val initialBinding = BindingSpec.of(binding)
        val newBinding = advancedBindingAdjustmentService.adjustBinding(
            initialBinding,
            binding.advancedBinding!!,
            fileContent,
            diffsByFile
        ).bindingSpec
        if (newBinding != initialBinding) {
            binding.update(newBinding.filePath, newBinding.startLine, newBinding.endLine)
        }
    }

    private fun handleBasicBinding(
        binding: Binding,
        diffsByFile: Map<String, List<Diff>>
    ) {
        val diffs: List<Diff> = diffsByFile[binding.filePath] ?: return
        val initialBinding = BindingSpec.of(binding)
        val result = bindingAdjustmentService.applyDiffs(initialBinding, diffs)
        val newBinding = result.bindingSpec
        if (newBinding != initialBinding) {
            binding.update(newBinding.filePath, newBinding.startLine, newBinding.endLine)
        }
    }
}