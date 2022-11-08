package dev.ithurts.api.web.page

import dev.ithurts.service.workspace.Workspace

abstract class SessionAwarePage {
    abstract val org: Workspace
    abstract val orgs: List<Workspace>
}