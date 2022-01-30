package dev.ithurts.application.service.advancedbinding

import dev.ithurts.application.dto.PushInfo
import dev.ithurts.application.service.codechange.trimDiffFilepath
import dev.ithurts.application.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.domain.Language
import dev.ithurts.domain.debt.Binding
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class AdvancedBindingAdjustmentService(
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val languageSpecificBindingServices: Map<Language, LanguageSpecificBindingService>
) {
    /**
     * @return true if the binding was adjusted, false otherwise / remove after adding debt events
     */

    fun adjustBinding(binding: Binding, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo): Boolean {
        if (!binding.isAdvanced()) {
            throw IllegalArgumentException("Binding is not advanced")
        }
        val bindingRelatedDiffs: List<Diff> = diffsByFile[binding.filePath] ?: return false
        val pathToFileBindingCurrentlyLocated = trimDiffFilepath(bindingRelatedDiffs.last().toFileName)
        val fileContent = downloadFileContent(pushInfo, pathToFileBindingCurrentlyLocated)
        val advancedBinding = binding.advancedBinding!!

        val bindingLineRange = languageSpecificBindingServices[advancedBinding.language]?.lookupBindingLocation(
            advancedBinding, fileContent
        ) ?: throw IllegalArgumentException("Language not supported")

        // todo consider as changed if there were change in a diff
        if (binding.filePath != pathToFileBindingCurrentlyLocated || binding.startLine != bindingLineRange.start || binding.endLine != bindingLineRange.end) {
            binding.update(pathToFileBindingCurrentlyLocated, bindingLineRange.start, bindingLineRange.end)
            return binding.endLine - binding.startLine != bindingLineRange.end - bindingLineRange.start
        }
        return false
    }

    private fun downloadFileContent(
        pushInfo: PushInfo, filePath: String
    ) = sourceProviderCommunicationService.getFile(
        pushInfo.workspaceExternalId, pushInfo.repositoryExternalId, filePath, pushInfo.commitHash
    )

}