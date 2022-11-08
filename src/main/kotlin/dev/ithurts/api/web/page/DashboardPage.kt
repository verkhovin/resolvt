package dev.ithurts.api.web.page

import dev.ithurts.service.workspace.Workspace

data class DashboardPage(
    override val org: Workspace,
    override val orgs: List<Workspace>
): SessionAwarePage()