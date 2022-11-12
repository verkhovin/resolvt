package dev.ithurts.service.sourceprovider.github.model

import com.fasterxml.jackson.annotation.JsonProperty

class GithubRepository(
    val name: String,
    @JsonProperty("default_branch")
    val defaultBranch: String
)