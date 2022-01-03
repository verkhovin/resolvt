package dev.ithurts.controller.web.page

import dev.ithurts.model.organisation.Organisation

abstract class SessionAwarePage {
    abstract val org: Organisation
    abstract val orgs: List<Organisation>
}