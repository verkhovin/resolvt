package dev.ithurts.application.internal

import dev.ithurts.application.internal.git.trimDiffFilepath
import dev.ithurts.application.model.PushInfo
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.debt.DiffApplier
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
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val diffApplier: DiffApplier
) {
    fun handleDiff(gitPatch: String, pushInfo: PushInfo) {
        if (gitPatch.isBlank()) {
            return
        }
        val parsedDiffs: List<Diff> = diffParser.parse(gitPatch.toByteArray())
        val debtsForChangedFiles = debtsForChangedFiles(parsedDiffs)
        val filePathToDiffs: Map<String, List<Diff>> = parsedDiffs.groupBy { trimDiffFilepath(it.fromFileName) }
        debtsForChangedFiles.forEach { debt ->
            val debtBindingChangedEvent = debt.applyDiffs(filePathToDiffs, pushInfo.commitHash, diffApplier)
            debtRepository.save(debtBindingChangedEvent.source)
            applicationEventPublisher.publishEvent(debtBindingChangedEvent)
        }
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