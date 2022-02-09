package dev.ithurts.application.service

import dev.ithurts.application.LineRange
import dev.ithurts.application.dto.PushInfo
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import dev.ithurts.application.service.advancedbinding.AdvancedBindingAdjustmentService
import dev.ithurts.application.service.codechange.trimDiffFilepath
import dev.ithurts.application.service.events.Change
import dev.ithurts.application.service.events.DebtBindingChangedEvent
import dev.ithurts.application.service.git.GitDiffAnalyzer
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.model.Diff
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class CodeChangeHandlingService(
    private val diffParser: DiffParser,
    private val debtRepository: DebtRepository,
    private val integrationAuthenticationFacade: IntegrationAuthenticationFacade,
    private val gitDiffAnalyzer: GitDiffAnalyzer,
    private val advancedBindingAdjustmentService: AdvancedBindingAdjustmentService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun handleDiff(gitPatch: String, pushInfo: PushInfo) {
        if (gitPatch.isBlank()) {
            return
        }
        val parsedDiffs: List<Diff> = diffParser.parse(gitPatch.toByteArray())
        val debtsForChangedFiles = debtsForChangedFiles(parsedDiffs)
        val filePathToDiffs: Map<String, List<Diff>> = parsedDiffs.groupBy { trimDiffFilepath(it.fromFileName) }
        debtsForChangedFiles.forEach { debt ->
            val changes = applyDiffs(debt, filePathToDiffs, pushInfo)
            applicationEventPublisher.publishEvent(
                debt.eventForChanges(changes, pushInfo.commitHash)
            )
            debtRepository.save(debt)
        }
    }

    private fun applyDiffs(debt: Debt, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo): List<Change> {
        return debt.bindings.map { binding ->
            if (binding.isAdvanced()) {
                advancedBindingAdjustmentService.adjustBinding(binding, diffsByFile, pushInfo)
            } else {
                adjustBasicBinding(binding, diffsByFile)
            }
        }.flatten()
    }

    /**
     * @return true if the binding was adjusted, false otherwise
     */
    private fun adjustBasicBinding(binding: Binding, diffsByFile: Map<String, List<Diff>>): List<Change> {
        val bindingRelatedDiffs: List<Diff> = diffsByFile[binding.filePath] ?: return emptyList()

        val currentBindingPosition = LineRange(binding.startLine, binding.endLine)
        val selectionChange = gitDiffAnalyzer.lookupSelectionChange(currentBindingPosition, bindingRelatedDiffs)
        val newFilePath = trimDiffFilepath(bindingRelatedDiffs.last().toFileName)
        val newBindingPosition: LineRange = selectionChange.position

        return binding.update(
            newFilePath,
            selectionChange.wasSelectedCodeChanged,
            newBindingPosition.start,
            newBindingPosition.end
        )
    }

    private fun debtsForChangedFiles(parsedDiffs: List<Diff>): List<Debt> {
        val changedFilePaths = parsedDiffs.map { trimDiffFilepath(it.fromFileName) }
        return debtRepository.findByWorkspaceIdAndBindingsFilePathInAndStatusNot(
            integrationAuthenticationFacade.workspace.id,
            changedFilePaths
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(CodeChangeHandlingService::class.java)
    }
}