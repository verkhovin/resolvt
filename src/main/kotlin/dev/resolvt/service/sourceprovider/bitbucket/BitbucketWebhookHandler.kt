package dev.resolvt.service.sourceprovider.bitbucket

import dev.resolvt.configuration.Bitbucket
import dev.resolvt.service.debt.model.PushInfo
import dev.resolvt.service.permission.IntegrationAuthenticationFacade
import dev.resolvt.service.sourceprovider.SourceProviderCommunicationService
import dev.resolvt.service.sourceprovider.bitbucket.model.BitbucketAppInstallation
import dev.resolvt.service.sourceprovider.bitbucket.model.ChangesPushed
import dev.resolvt.service.sourceprovider.bitbucket.model.RepoUpdated
import dev.resolvt.service.SourceProvider
import dev.resolvt.service.account.AccountRepository
import dev.resolvt.service.debt.diff.DiffApplyingService
import dev.resolvt.service.repository.RepositoryRepository
import dev.resolvt.service.workspace.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
@Bitbucket
class BitbucketWebhookHandler(
    private val accountRepository: AccountRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val repositoryRepository: RepositoryRepository,
    private val authenticationFacade: IntegrationAuthenticationFacade,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val diffApplyingService: DiffApplyingService
) {
    fun appInstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val actorAccount = accountRepository.findByExternalIdAndSourceProvider(
            "{${bitbucketAppInstallation.actor!!.uuid}}",
            SourceProvider.BITBUCKET
        ) ?: throw IllegalArgumentException("Actor is not Resolvt account")
        val bitbucketWorkspace = bitbucketAppInstallation.principal
        val sourceProviderApplicationCredentials = SourceProviderApplicationCredentials.from(
            bitbucketAppInstallation.clientKey!!,
            bitbucketAppInstallation.sharedSecret!!
        )

        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            SourceProvider.BITBUCKET,
            bitbucketWorkspace.bitbucketId
        )?.connectWithSourceProviderApplication(sourceProviderApplicationCredentials)
            ?: Workspace(
                bitbucketWorkspace.displayName,
                SourceProvider.BITBUCKET,
                WorkspaceType.ORGANISATION,
                bitbucketWorkspace.username ?: bitbucketWorkspace.nickname!!,
                sourceProviderApplicationCredentials,
                true,
                listOf(WorkspaceMember(actorAccount.id, WorkspaceMemberRole.ADMIN, WorkspaceMemberStatus.ACTIVE))
            )
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(workspace, null, emptyList())
        sourceProviderCommunicationService.checkIsMember(workspace, actorAccount)
        workspaceRepository.save(workspace)
    }

    fun appUninstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val workspace = workspaceRepository.findBySourceProviderAndExternalId(
            SourceProvider.BITBUCKET,
            bitbucketAppInstallation.principal.bitbucketId
        ) ?: return
        workspaceRepository.save(workspace.deactivate())
    }

    fun repoUpdated(data: RepoUpdated) {
        val workspace = authenticationFacade.workspace
        val oldName = data.changes.slug.old
        val newName = data.changes.slug.new
        repositoryRepository.findByNameAndWorkspaceId(oldName, workspace.id)?.let { repository ->
            repositoryRepository.save(repository.rename(newName))
        }
    }

    fun changesPushed(changesPushedEvent: ChangesPushed) {
        val repository = repositoryRepository.findByNameAndWorkspaceId(
            changesPushedEvent.repository.name,
            authenticationFacade.workspace.id
        ) ?: return
        changesPushedEvent.push.changes.forEach { change ->
            val diffSpec = "${change.new.target.hash}..${change.old.target.hash}"
            val diff = sourceProviderCommunicationService.getDiff(
                changesPushedEvent.repository.workspace.slug,
                changesPushedEvent.repository.name,
                diffSpec
            )
            diffApplyingService.applyDiff(
                diff,
                PushInfo(
                    change.new.target.hash,
                    repository.id,
                    changesPushedEvent.repository.workspace.slug,
                    changesPushedEvent.repository.name
                )
            )
        }
    }
}
