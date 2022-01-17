package dev.ithurts.application.service

import dev.ithurts.application.security.AuthenticationFacade
import dev.ithurts.exception.DebtReportFailedException
import dev.ithurts.application.dto.TechDebtReport
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.domain.repository.RepositoryService
import org.springframework.stereotype.Service

@Service
class DebtApplicationService(
    private val repositoryService: RepositoryService,
    private val debtRepository: DebtRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val repositoryInfoService: RepositoryInfoService,
    private val authenticationFacade: AuthenticationFacade,
) {
    fun createDebt(techDebtReport: TechDebtReport): Long {
        val repositoryInfo: RepositoryInfo = repositoryInfoService.parseRemoteUrl(techDebtReport.remoteUrl)
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            repositoryInfo.sourceProvider,
            repositoryInfo.workspaceExternalId
        ) ?: throw DebtReportFailedException("No organisation found for ${repositoryInfo.workspaceExternalId}")

        val repository = repositoryRepository.findByNameAndWorkspaceId(repositoryInfo.name, workspace.identity)
            ?: repositoryService.acknowledgeExternalRepositoryByWorkspace(workspace, repositoryInfo.name).let {
                repositoryRepository.save(it)
            }

        val debt = repository.reportDebt(techDebtReport, authenticationFacade.account.identity)
        return debtRepository.save(debt).identity
    }

}