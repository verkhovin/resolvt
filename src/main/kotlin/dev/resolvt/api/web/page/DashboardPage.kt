package dev.resolvt.api.web.page

import dev.resolvt.service.workspace.Workspace

data class DashboardPage(
    override val org: Workspace,
    override val orgs: List<Workspace>
): SessionAwarePage()