package dev.ithurts.controller.web.page

import dev.ithurts.model.organisation.Organisation

data class DashboardPage(
    override val org: Organisation,
    override val orgs: List<Organisation>
): SessionAwarePage()