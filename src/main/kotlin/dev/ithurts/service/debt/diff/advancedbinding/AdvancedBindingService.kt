package dev.ithurts.service.debt.diff.advancedbinding

import dev.ithurts.service.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.application.model.LineRange
import dev.ithurts.service.Language
import dev.ithurts.service.debt.model.AdvancedBinding
import dev.ithurts.service.debt.model.Debt
import dev.ithurts.service.repository.RepositoryRepository
import dev.ithurts.application.exception.EntityNotFoundException
import dev.ithurts.service.workspace.WorkspaceService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdvancedBindingService(
    private val languageSpecificBindingServices: Map<Language, LanguageSpecificBindingService>,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val workspaceService: WorkspaceService,
    private val repositoryRepository: RepositoryRepository
) {
    fun lookupBindingLocation(debt: Debt, advancedBinding: AdvancedBinding, filePath: String, commitHashOrBranch: String): LineRange? {
        val languageSpecificBindingService = languageSpecificBindingServices[advancedBinding.language]
        if (languageSpecificBindingService == null) {
            log.error("Didn't find appropriate handler for language ${advancedBinding.language}")
            throw Exception("Didn't find appropriate handler for language ${advancedBinding.language}")
        }
        val repo = repositoryRepository.findById(debt.repositoryId).orElseThrow{ EntityNotFoundException("Repository", "id", debt.repositoryId) }
        val workspace = workspaceService.getWorkspaceById(repo.workspaceId)
        val file = sourceProviderCommunicationService.getFile(workspace, repo.name, filePath, commitHashOrBranch)
        return languageSpecificBindingService.lookupBindingLocation(advancedBinding, file)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AdvancedBindingService::class.java)
    }
}