package dev.ithurts.controller.web.page

import dev.ithurts.domain.workspace.Workspace

abstract class SessionAwarePage {
    abstract val org: Workspace
    abstract val orgs: List<Workspace>
}