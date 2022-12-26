package dev.resolvt.service.sourceprovider.github.model

import com.fasterxml.jackson.annotation.JsonProperty

class GithubRepositoryInfo(
    val name: String,
    @JsonProperty("default_branch")
    val defaultBranch: String
)