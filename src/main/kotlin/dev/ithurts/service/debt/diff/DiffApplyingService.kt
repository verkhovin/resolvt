package dev.ithurts.service.debt.diff

import dev.ithurts.service.debt.model.PushInfo
import dev.ithurts.application.model.end
import dev.ithurts.application.model.start
import dev.ithurts.service.permission.IntegrationAuthenticationFacade
import dev.ithurts.application.service.internal.diff.trimDiffFilepath
import dev.ithurts.git.DiffDirection
import dev.ithurts.git.GitDiffAnalyzer
import dev.ithurts.git.LineRange
import dev.ithurts.service.debt.DebtRepository
import dev.ithurts.service.debt.diff.advancedbinding.AdvancedBindingService
import dev.ithurts.service.debt.model.*
import dev.ithurts.service.debt.debtevent.BindingChange
import dev.ithurts.service.debt.debtevent.ChangeType
import dev.ithurts.service.debt.debtevent.DebtEvent
import dev.ithurts.service.debt.debtevent.DebtEventService
import io.reflectoring.diffparser.api.DiffParser
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class DiffApplyingService(
    private val diffParser: DiffParser,
    private val debtRepository: DebtRepository,
    private val integrationAuthenticationFacade: IntegrationAuthenticationFacade,
    private val gitDiffAnalyzer: GitDiffAnalyzer,
    private val advancedBindingService: AdvancedBindingService,
    private val debtEventService: DebtEventService,
) {

    fun applyDiff(gitPatch: String, pushInfo: PushInfo) {
        if (gitPatch.isBlank()) {
            return
        }
        val parsedDiffs: List<Diff> = diffParser.parse(gitPatch.toByteArray())
        val debtsForChangedFiles = debtsForChangedFiles(parsedDiffs)
        val filePathToDiffs: Map<String, List<Diff>> = parsedDiffs.groupBy { trimDiffFilepath(it.fromFileName) }
        debtsForChangedFiles.forEach { debt ->
            applyDiffs(debt, filePathToDiffs, pushInfo.commitHash)
        }
    }

    private fun debtsForChangedFiles(parsedDiffs: List<Diff>): List<Debt> {
        val changedFilePaths = parsedDiffs.map { trimDiffFilepath(it.fromFileName) }
        return debtRepository.findByWorkspaceIdAndBindingsFilePathInAndStatusNot(
            integrationAuthenticationFacade.workspace.id,
            changedFilePaths
        )
    }

    private fun applyDiffs(
        debt: Debt,
        diffs: Map<String, List<Diff>>,
        commitHash: String,
    ) {
        val activeBindings = debt.bindings.filter { it.status != BindingStatus.ARCHIVED }
        val applyingResultByBindingId = activeBindings.map { binding ->
            applyDiffToBinding(debt, binding, diffs[binding.filePath], commitHash)
        }.associateBy { it.binding.id }
        val changes = deriveChanges(debt, applyingResultByBindingId)
        debtRepository.save(
            debt.copy(
                bindings = debt.bindings.map { binding -> applyingResultByBindingId[binding.id]?.binding ?: binding }
            )
        )
        debtEventService.saveEvent(DebtEvent(debt.id, debt.repositoryId, commitHash, changes))
    }

    private fun deriveChanges(debt: Debt,
        applyingResultByBindingId: Map<String, DiffApplyingResult>,
    ): List<BindingChange> = debt.bindings.flatMap { binding ->
        val diffApplyingResult = applyingResultByBindingId[binding.id]
        val newBinding = diffApplyingResult?.binding ?: return@flatMap emptyList()

        val bindingChanges = mutableListOf<BindingChange>()

        if (newBinding.status == BindingStatus.TRACKING_LOST) {
            bindingChanges.add(trackingLostBindingChange(binding))
            return@flatMap bindingChanges
        }

        if (binding.filePath != newBinding.filePath) {
            bindingChanges.add(fileMovedBindingChange(binding, newBinding))
        }
        if (diffApplyingResult.codeChanged || binding.startLine - binding.endLine != newBinding.startLine - newBinding.endLine) {
            bindingChanges.add(codeChangedBindingChange(binding, newBinding))
        } else if (newBinding.startLine != binding.startLine || newBinding.endLine != binding.endLine) {
            bindingChanges.add(codeMovedBindingChange(binding, newBinding))
        }

        val changeTypes = bindingChanges.map { it.type }.distinct()
        if (changeTypes.size != bindingChanges.size) {
            throw IllegalStateException("Binding change types duplicated: ${binding.id}}: ${changeTypes}")
        }

        bindingChanges
    }

    private fun applyDiffToBinding(debt: Debt, binding: Binding, diffs: List<Diff>?, commitHash: String): DiffApplyingResult {
        if(diffs == null || diffs.isEmpty()) return DiffApplyingResult(binding, codeChanged = false)
        val currentBindingPosition = LineRange(binding.startLine, binding.endLine)
        val newFilePath = trimDiffFilepath(diffs.last().toFileName)

        val (newBindingPosition, codeChanged) = if (binding.isAdvanced()) {
            val advancedBinding = binding.advancedBinding!!
            val newLocation =
                advancedBindingService.lookupBindingLocation(debt, advancedBinding, newFilePath, commitHash)
                    ?: return DiffApplyingResult(binding.copy(status = BindingStatus.TRACKING_LOST), codeChanged = false)
            newLocation to hasCodeChangeInsideAdvancedBinding(newLocation, diffs)
        } else {
            val selectionChange = gitDiffAnalyzer.lookupCodeRangeChange(currentBindingPosition, diffs)
            selectionChange.position to selectionChange.wasSelectedCodeChanged
        }

        return DiffApplyingResult(
            binding.copy(
                filePath = newFilePath,
                startLine = newBindingPosition.start,
                endLine = newBindingPosition.end
            ),
            codeChanged
        )
    }

    private fun fileMovedBindingChange(
        binding: Binding,
        newBinding: Binding,
    ) = BindingChange(binding.id,
        ChangeType.FILE_MOVED,
        binding.filePath,
        newBinding.filePath,
        visible = true)

    private fun codeMovedBindingChange(
        binding: Binding,
        newBinding: Binding,
    ) = BindingChange(
        binding.id,
        ChangeType.CODE_MOVED,
        "${binding.startLine}:${binding.endLine}",
        "${newBinding.startLine}:${newBinding.endLine}",
        visible = false
    )

    private fun codeChangedBindingChange(
        binding: Binding,
        newBinding: Binding,
    ) = BindingChange(
        binding.id,
        ChangeType.CODE_CHANGED,
        "${binding.startLine}:${binding.endLine}",
        "${newBinding.startLine}:${newBinding.endLine}",
        visible = true
    )

    private fun trackingLostBindingChange(binding: Binding) =
        BindingChange(binding.id, ChangeType.ADVANCED_BINDING_TARGET_LOST, null, null, visible = true)

    private fun hasCodeChangeInsideAdvancedBinding(
        newBindingLocation: LineRange,
        diffs: List<Diff>,
    ): Boolean = gitDiffAnalyzer.lookupCodeRangeChange(newBindingLocation, diffs, DiffDirection.REVERSE)
        .wasSelectedCodeChanged

}

class DiffApplyingResult(
    val binding: Binding,
    val codeChanged: Boolean,
)