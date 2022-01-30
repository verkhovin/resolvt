package dev.ithurts.application.service

import dev.ithurts.application.LineRange
import dev.ithurts.application.dto.PushInfo
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import dev.ithurts.application.service.advancedbinding.AdvancedBindingAdjustmentService
import dev.ithurts.application.service.codechange.trimDiffFilepath
import dev.ithurts.application.service.git.GitDiffAnalyzer
import dev.ithurts.domain.debt.Binding
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.model.Diff
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CodeChangeHandlingService(
    private val diffParser: DiffParser,
    private val debtRepository: DebtRepository,
    private val integrationAuthenticationFacade: IntegrationAuthenticationFacade,
    private val gitDiffAnalyzer: GitDiffAnalyzer,
    private val advancedBindingAdjustmentService: AdvancedBindingAdjustmentService
) {
    fun handleDiff(gitPatch: String, pushInfo: PushInfo) {
        if (gitPatch.isBlank()) {
            return
        }
        val parsedDiffs: List<Diff> = diffParser.parse(gitPatch.toByteArray())
        val debtsForChangedFiles = debtsForChangedFiles(parsedDiffs)
        val filePathToDiffs: Map<String, List<Diff>> = parsedDiffs.groupBy { trimDiffFilepath(it.fromFileName) }
        debtsForChangedFiles.forEach { debt ->
            applyDiffs(debt, filePathToDiffs, pushInfo)
            debtRepository.save(debt)
        }
    }

    private fun applyDiffs(debt: Debt, diffsByFile: Map<String, List<Diff>>, pushInfo: PushInfo) {
        debt.bindings.forEach { binding ->
            if (binding.isAdvanced()) {
                advancedBindingAdjustmentService.adjustBinding(binding, diffsByFile, pushInfo)
            } else {
                adjustBasicBinding(binding, diffsByFile)
            }
        }
    }

    private fun adjustBasicBinding(binding: Binding, diffsByFile: Map<String, List<Diff>>) {
        val bindingRelatedDiffs: List<Diff> = diffsByFile[binding.filePath] ?: return

        val currentBindingPosition = LineRange(binding.startLine, binding.endLine)
        val newPosition = gitDiffAnalyzer.calculateSelectionMovement(currentBindingPosition, bindingRelatedDiffs)
        val newFilePath = trimDiffFilepath(bindingRelatedDiffs.last().toFileName)

        if (newFilePath != binding.filePath || currentBindingPosition != newPosition) {
            binding.update(newFilePath, newPosition.start, newPosition.end)
        }
    }


    private fun debtsForChangedFiles(parsedDiffs: List<Diff>): List<Debt> {
        val changedFilePaths = parsedDiffs.map { trimDiffFilepath(it.fromFileName) }
        return debtRepository.findByWorkspaceIdAndFilePaths(
            integrationAuthenticationFacade.workspace.identity,
            changedFilePaths
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(CodeChangeHandlingService::class.java)
    }
}