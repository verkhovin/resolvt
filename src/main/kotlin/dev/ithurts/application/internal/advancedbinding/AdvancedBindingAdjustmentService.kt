package dev.ithurts.application.internal.advancedbinding

import dev.ithurts.application.model.PushInfo
import dev.ithurts.application.internal.git.trimDiffFilepath
import dev.ithurts.application.events.Change
import dev.ithurts.application.internal.sourceprovider.SourceProviderCommunicationService
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

    fun adjustBinding(binding: Binding, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo): List<Change> {
        if (!binding.isAdvanced()) {
            throw IllegalArgumentException("Binding is not advanced")
        }
        val bindingRelatedDiffs: List<Diff> = diffsByFile[binding.filePath] ?: return emptyList()
        val pathToFileBindingCurrentlyLocated = trimDiffFilepath(bindingRelatedDiffs.last().toFileName)
        val fileContent = downloadFileContent(pushInfo, pathToFileBindingCurrentlyLocated)
        val advancedBinding = binding.advancedBinding!!

        val bindingLineRange = languageSpecificBindingServices[advancedBinding.language]?.lookupBindingLocation(
            advancedBinding, fileContent
        ) ?: throw IllegalArgumentException("Language not supported")

        return binding.update(
            pathToFileBindingCurrentlyLocated,
            false, // todo consider as changed if there were changes in a diff, do not base on line range
            bindingLineRange.start,
            bindingLineRange.end
        )
    }

    private fun downloadFileContent(
        pushInfo: PushInfo, filePath: String
    ) = sourceProviderCommunicationService.getFile(
        pushInfo.workspaceExternalId, pushInfo.repositoryExternalId, filePath, pushInfo.commitHash
    )

}