package dev.ithurts.sourceprovider.bitbucket.dto.webhook

import com.fasterxml.jackson.annotation.JsonProperty

data class ChangesPushed(
    val push: Push,
    val repository: Repository
)

data class Push(
    val changes: List<Change>,
)

data class Repository(
    val name: String,
    val workspace: Workspace
)

data class Workspace(
    val slug: String
)

data class Change(
    val forced: Boolean,
    val old: RepoHistoryState,
    val new: RepoHistoryState
)

data class RepoHistoryState(
    @JsonProperty("name")
    val branchName: String,
    val target: StateTarget,
)

data class StateTarget(
    val hash: String
)
