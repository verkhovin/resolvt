package dev.ithurts.application.service

import dev.ithurts.controller.api.webhook.dto.BitbucketAppInstallation
import dev.ithurts.controller.api.webhook.dto.ChangesPushed
import dev.ithurts.controller.api.webhook.dto.RepoUpdated
import dev.ithurts.domain.SourceProvider
import dev.ithurts.domain.SourceProviderWorkspace
import dev.ithurts.domain.workspace.SourceProviderApplicationCredentials
import dev.ithurts.domain.workspace.WorkspaceFactory
import dev.ithurts.domain.account.AccountRepository
import dev.ithurts.domain.repository.RepositoryRepository
import dev.ithurts.domain.workspace.WorkspaceRepository
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import dev.ithurts.application.service.diff.DiffHandlingService
import dev.ithurts.application.sourceprovider.SourceProviderCommunicationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BitbucketWebhookHandler(
    private val accountRepository: AccountRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val authenticationFacade: IntegrationAuthenticationFacade,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val diffHandlingService: DiffHandlingService
) {
    fun appInstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val actorAccount = accountRepository.findByExternalIdAndSourceProvider(
            "{${bitbucketAppInstallation.actor!!.uuid}}",
            SourceProvider.BITBUCKET
        ) ?: throw IllegalArgumentException("Actor is not It Hurts account")
        val bitbucketWorkspace = bitbucketAppInstallation.principal
        val sourceProviderApplicationCredentials = SourceProviderApplicationCredentials.from(
            bitbucketAppInstallation.clientKey!!,
            bitbucketAppInstallation.sharedSecret!!
        )

        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            SourceProvider.BITBUCKET,
            bitbucketWorkspace.bitbucketId
        )

        if (workspace != null) {
            workspace.connectWithSourceProviderApplication(sourceProviderApplicationCredentials)
            workspaceRepository.save(workspace)
        } else {
            WorkspaceFactory.fromBitbucketWorkspace(
                actorAccount.identity, SourceProviderWorkspace(
                    bitbucketWorkspace.username ?: bitbucketWorkspace.nickname!!,
                    bitbucketWorkspace.displayName,
                    SourceProvider.BITBUCKET
                ), sourceProviderApplicationCredentials
            ).let { workspaceRepository.save(it) }
        }
    }

    fun appUninstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            SourceProvider.BITBUCKET,
            bitbucketAppInstallation.principal.bitbucketId
        ) ?: return

        workspace.deactivate()

        workspaceRepository.save(workspace)
    }

    fun repoUpdated(data: RepoUpdated) {
        val workspace = authenticationFacade.workspace
        val oldName = data.changes.slug.old
        val newName = data.changes.slug.new
        repositoryRepository.findByNameAndWorkspaceId(oldName, workspace.identity)?.let { repository ->
                repository.rename(newName)
                repositoryRepository.save(repository)
            }
    }

    fun changesPushed(changesPushedEvent: ChangesPushed) {
        repositoryRepository.findByNameAndWorkspaceId(changesPushedEvent.repository.name, authenticationFacade.workspace.identity)
            ?: return
        changesPushedEvent.push.changes.forEach { change ->
            val diffSpec = "${change.new.target.hash}..${change.old.target.hash}"
            val diff = sourceProviderCommunicationService.getDiff(
                changesPushedEvent.repository.workspace.slug,
                changesPushedEvent.repository.name,
                diffSpec
            )
            log.info(diff)
            diffHandlingService.handleDiff(diff)
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(BitbucketWebhookHandler::class.java)
    }
}