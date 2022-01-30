package dev.ithurts.application.service.codechange

import dev.ithurts.application.dto.PushInfo
import dev.ithurts.application.service.code.CodeAnalyzer
import dev.ithurts.application.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.domain.debt.Binding
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class AdvancedBindingAdjustmentService(
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val codeAnalyzer: CodeAnalyzer
) {
    fun adjustBinding(binding: Binding, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo) {
        if (!binding.isAdvanced()) {
            throw IllegalArgumentException("Binding is not advanced")
        }
        val bindingRelatedDiffs: List<Diff> = diffsByFile[binding.filePath] ?: return
        val pathToFileBindingCurrentlyLocated = trimDiffFilepath(bindingRelatedDiffs.last().toFileName)
        val fileContent = downloadFileContent(pushInfo, pathToFileBindingCurrentlyLocated)
        val advancedBinding = binding.advancedBinding!!
        val matchedCodeEntities = codeAnalyzer.find(advancedBinding.name, advancedBinding.type, advancedBinding.language, fileContent)

        val entity = matchedCodeEntities.asSequence()
            // FIXME actually, class could be renamed. in this case binding will be lost. System of assumptions could help here.
            .filter { it.parent?.name == simpleClassName(advancedBinding.parent) }
            // Well, the number of parameters or their type could change. If we know that the function hasn't had overrides,
            // we can quite safely assume that the parameters were changed, and we didn't found some override here
            .first { function ->
                function.parameters == advancedBinding.params.map(::simpleClassName)
            }
        binding.update(pathToFileBindingCurrentlyLocated, entity.lines.start, entity.lines.end)
    }

    private fun downloadFileContent(
        pushInfo: PushInfo,
        filePath: String
    ) = sourceProviderCommunicationService.getFile(
        pushInfo.workspaceExternalId,
        pushInfo.repositoryExternalId,
        filePath,
        pushInfo.commitHash
    )

    private fun simpleClassName(name: String?): String? {
        return name?.substringAfterLast(".")
    }
}