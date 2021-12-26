package dev.ithurts.sourceprovider.bitbucket

import dev.ithurts.model.SourceProvider
import dev.ithurts.controller.api.webhook.dto.BitbucketAppInstallation
import dev.ithurts.repository.AccountRepository
import dev.ithurts.security.IntegrationAuthenticationFacade
import dev.ithurts.service.diff.DiffHandlingService
import dev.ithurts.service.OrganisationApiService
import dev.ithurts.service.core.RepositoryService
import dev.ithurts.sourceprovider.SourceProviderCommunicationService
import dev.ithurts.controller.api.webhook.dto.ChangesPushed
import dev.ithurts.controller.api.webhook.dto.RepoUpdated
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BitbucketWebhookHandler(
    private val accountRepository: AccountRepository,
    private val organisationApiService: OrganisationApiService,
    private val repositoryService: RepositoryService,
    private val authenticationFacade: IntegrationAuthenticationFacade,
    private val sourceProviderCommunicationService: SourceProviderCommunicationService,
    private val diffHandlingService: DiffHandlingService
) {
    fun appInstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val actorAccount = accountRepository.findByExternalIdAndSourceProvider(
            "{${bitbucketAppInstallation.actor!!.uuid}}",
            SourceProvider.BITBUCKET
        ) ?: throw IllegalArgumentException("Actor is not It Hurts account")

        val organisation = bitbucketAppInstallation.principal
        organisationApiService.createOrganisationFromExternalOne(
            SourceProviderOrganisation(
                organisation.username,
                organisation.displayName,
                SourceProvider.BITBUCKET
            ),
            actorAccount,
            bitbucketAppInstallation.clientKey!!,
            bitbucketAppInstallation.sharedSecret!!
        )
    }

    fun appUninstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        organisationApiService.deactivateOrganisation(
            SourceProvider.BITBUCKET,
            bitbucketAppInstallation.principal.username
        )
    }

    fun repoUpdated(data: RepoUpdated) {
        val organisation = authenticationFacade.organisation
        repositoryService.changeName(organisation, data.changes.slug.old, data.changes.slug.new)
    }

    fun changesPushed(changesPushedEvent: ChangesPushed) {
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
        val log = LoggerFactory.getLogger(BitbucketWebhookHandler::class.java)
    }
}