package dev.resolvt.service.sourceprovider

import dev.resolvt.service.SourceProvider

class SourceProviderIntegrationDisabledException(sourceProvider: SourceProvider) :
    Exception("${sourceProvider.name} integration disabled. Please, check 'resolvt.source-providers.enabled' property of Resolvt instance.")