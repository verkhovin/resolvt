package dev.ithurts.sourceprovider.bitbucket

import dev.ithurts.model.SourceProvider
import dev.ithurts.sourceprovider.SourceProviderClient
import dev.ithurts.sourceprovider.bitbucket.dto.BitbucketUserEmailInfo
import dev.ithurts.sourceprovider.bitbucket.dto.BitbucketWorkspace
import dev.ithurts.sourceprovider.bitbucket.dto.Values
import dev.ithurts.sourceprovider.model.SourceProviderOrganisation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange

@Service
class BitbucketClient(
    @Qualifier("bitbucketRestTemplate") val restTemplate: RestOperations
) : SourceProviderClient {
    override val organisationOwnerRole: String = "owner"

    override fun getUserOrganisations(accessToken: String, role: String): List<SourceProviderOrganisation> {
        val workspaces = restTemplate.exchange<Values<BitbucketWorkspace>>(
            "/workspaces?role={role}",
            HttpMethod.GET,
            noBody(accessToken),
            role
        ).body!!.values
        return workspaces.map { workspace ->
            workspaceToSourceProviderOrganisation(workspace)
        }
    }

    override fun getOrganisation(accessToken: String, organisationId: String): SourceProviderOrganisation {
        val workspace = restTemplate.exchange<BitbucketWorkspace>(
            "/workspaces/{id}",
            HttpMethod.GET,
            noBody(accessToken),
            organisationId
        ).body!!
        return workspaceToSourceProviderOrganisation(workspace)
    }

    fun getUserPrimaryEmail(accessToken: String): String {
        val emails: List<BitbucketUserEmailInfo> = restTemplate.exchange<Values<BitbucketUserEmailInfo>>(
            "/user/emails",
            HttpMethod.GET,
            noBody(accessToken)
        ).body!!.values
        return (emails.firstOrNull { it.isPrimary } ?: emails[0]).email
    }

    private fun workspaceToSourceProviderOrganisation(workspace: BitbucketWorkspace) =
        SourceProviderOrganisation(workspace.slug, workspace.name, SourceProvider.BITBUCKET)

    private fun noBody(accessToken: String) = HttpEntity(null, authorizationHeader(accessToken))

    private fun authorizationHeader(accessToken: String): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(accessToken ?: "")
        return httpHeaders
    }
}