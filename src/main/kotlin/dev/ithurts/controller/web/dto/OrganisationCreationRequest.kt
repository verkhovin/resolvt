package dev.ithurts.controller.web.dto

import dev.ithurts.model.SourceProvider

class OrganisationCreationRequest (
    val externalOrganisationId: String,
    val sourceProvider: SourceProvider
)