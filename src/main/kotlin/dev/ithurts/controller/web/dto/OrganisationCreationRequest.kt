package dev.ithurts.controller.web.dto

import dev.ithurts.domain.SourceProvider

class OrganisationCreationRequest (
    val externalOrganisationId: String,
    val sourceProvider: SourceProvider
)