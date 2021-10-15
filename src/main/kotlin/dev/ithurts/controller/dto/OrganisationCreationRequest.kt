package dev.ithurts.controller.dto

import dev.ithurts.model.SourceProvider

class OrganisationCreationRequest (
    val externalOrganisationId: String,
    val sourceProvider: SourceProvider
)