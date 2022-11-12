package dev.ithurts.service.sourceprovider.github.model

import com.fasterxml.jackson.annotation.JsonProperty

class GithubWebhookEvent(
    val action: String?,
    val installation: GithubAppInstallation?,
    val sender: GithubAppInstallationSender?,
    val before: String?,
    val after: String?,
    val repository: GitHubRepository?
)

class GithubAppInstallation(
    val id: Int,
    val account: GithubAppInstallationAccount?,
    @JsonProperty("target_type")
    val targetType: String?
)

class GithubAppInstallationAccount(
    val id: Int,
    val login: String
)

class GithubAppInstallationSender(
    val id: Int,
    val login: String,
)

class GitHubRepository(
    val name: String,
    val owner: GitHubRepositoryOwner
)

class GitHubRepositoryOwner(
    val name: String,
    val id: String
)
