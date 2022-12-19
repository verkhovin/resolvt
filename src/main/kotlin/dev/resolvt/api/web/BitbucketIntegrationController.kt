package dev.resolvt.api.web

import dev.resolvt.configuration.Bitbucket
import dev.resolvt.service.sourceprovider.bitbucket.BitbucketDescriptorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Bitbucket
class BitbucketIntegrationController(
    private val bitbucketDescriptorService: BitbucketDescriptorService
) {
    @GetMapping("/connect/bitbucket/descriptor", produces = ["application/json"])
    fun bitbucketDescriptor(): String {
        return bitbucketDescriptorService.getBitbucketConnectDescriptor()
    }
}