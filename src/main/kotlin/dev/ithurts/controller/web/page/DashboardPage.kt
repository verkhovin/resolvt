package dev.ithurts.controller.web.page

import dev.ithurts.domain.workspace.Workspace

data class DashboardPage(
    override val org: Workspace,
    override val orgs: List<Workspace>
): SessionAwarePage()