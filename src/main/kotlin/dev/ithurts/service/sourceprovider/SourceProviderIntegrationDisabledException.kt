package dev.ithurts.service.sourceprovider

import dev.ithurts.service.SourceProvider

class SourceProviderIntegrationDisabledException(sourceProvider: SourceProvider) :
    Exception("${sourceProvider.name} integration disabled. Please, check 'ithurts.source-providers.enabled' property of It Hurts instance.")