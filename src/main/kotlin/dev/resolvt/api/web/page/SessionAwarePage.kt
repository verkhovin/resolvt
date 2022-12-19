package dev.resolvt.api.web.page

import dev.resolvt.service.workspace.Workspace

abstract class SessionAwarePage {
    abstract val org: Workspace
    abstract val orgs: List<Workspace>
}