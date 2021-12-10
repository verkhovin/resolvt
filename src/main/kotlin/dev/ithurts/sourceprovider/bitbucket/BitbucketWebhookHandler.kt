package dev.ithurts.sourceprovider.bitbucket

import dev.ithurts.model.SourceProvider
import dev.ithurts.model.api.bitbucket.BitbucketAppInstallation
import dev.ithurts.repository.AccountRepository
import dev.ithurts.service.OrganisationService
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.stereotype.Service

@Service
class BitbucketWebhookHandler(
    private val accountRepository: AccountRepository,
    private val organisationService: OrganisationService
) {
    fun appInstalled(bitbucketAppInstallation: BitbucketAppInstallation) {
        val actorAccount = accountRepository.findByExternalIdAndSourceProvider(
            "{${bitbucketAppInstallation.actor!!.uuid}}",
            SourceProvider.BITBUCKET
        ) ?: throw IllegalArgumentException("Actor is not It Hurts account")

        val organisation = bitbucketAppInstallation.principal
        organisationService.createOrganisationFromExternalOne(
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
        organisationService.deactivateOrganisation(
            SourceProvider.BITBUCKET,
            bitbucketAppInstallation.principal.username
        )
    }
}