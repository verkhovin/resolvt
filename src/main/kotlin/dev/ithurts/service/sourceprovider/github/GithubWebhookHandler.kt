package dev.ithurts.service.sourceprovider.github

import dev.ithurts.configuration.Github
import dev.ithurts.service.SourceProvider
import dev.ithurts.service.account.AccountRepository
import dev.ithurts.service.debt.diff.DiffApplyingService
import dev.ithurts.service.debt.model.PushInfo
import dev.ithurts.service.repository.RepositoryRepository
import dev.ithurts.service.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.service.sourceprovider.github.model.GithubWebhookEvent
import dev.ithurts.service.workspace.*
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
@Github
class GithubWebhookHandler(
    private val accountRepository: AccountRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val diffApplyingService: DiffApplyingService,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService
) {
    fun appInstalled(githubAppInstallation: GithubWebhookEvent) {
        val senderAccount = accountRepository.findByExternalIdAndSourceProvider(
            githubAppInstallation.sender!!.login,
            SourceProvider.GITHUB
        ) ?: throw IllegalArgumentException("Actor is not It Hurts account")

        val sourceProviderApplicationCredentials = SourceProviderApplicationCredentials
            .from(githubAppInstallation.installation!!.id, null)

        val workspaceInfo = githubAppInstallation.installation.account!!
        val workspaceType = when (githubAppInstallation.installation.targetType) {
            "User" -> WorkspaceType.ACCOUNT
            "Organization" -> WorkspaceType.ORGANISATION
            else -> {
                log.error("Unsupported workspaceType, falling back to Organisation for ${workspaceInfo.id} ${workspaceInfo.login}")
                WorkspaceType.ORGANISATION
            }
        }
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            SourceProvider.GITHUB,
            workspaceInfo.login
        )?.connectWithSourceProviderApplication(sourceProviderApplicationCredentials)
            ?: Workspace(
                workspaceInfo.login,
                SourceProvider.GITHUB,
                workspaceType,
                workspaceInfo.login,
                sourceProviderApplicationCredentials,
                true,
                listOf(WorkspaceMember(senderAccount.id, WorkspaceMemberRole.ADMIN, WorkspaceMemberStatus.ACTIVE))
            )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(workspace, null, emptyList())
        sourceProviderCommunicationService.checkIsMember(workspace, senderAccount)
        workspaceRepository.save(workspace)
    }

    fun changesPushed(githubChangesPushed: GithubWebhookEvent) {
        val workspaceExternalId = githubChangesPushed.repository!!.owner.name
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            SourceProvider.GITHUB,
            workspaceExternalId
        ) ?: return
        val installationId = githubChangesPushed.installation!!.id
        if (workspace.sourceProviderApplicationCredentials.clientKey != installationId) {
            log.warn("Key saved in the database is not equal to the installation id provided. Workspace ${workspace.id}, " +
                    "installation id: $installationId")
        }

        val repository = repositoryRepository.findByNameAndWorkspaceId(
            githubChangesPushed.repository.name,
            workspace.id
        ) ?: return
        val diff = sourceProviderCommunicationService.getDiff(workspaceExternalId, repository.name,
            "${githubChangesPushed.before}...${githubChangesPushed.after!!}")
        diffApplyingService.applyDiff(
            diff,
            PushInfo(
                githubChangesPushed.after,
                repository.id,
                workspaceExternalId,
                githubChangesPushed.repository.name
            )
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(GithubWebhookHandler::class.java)
    }
}