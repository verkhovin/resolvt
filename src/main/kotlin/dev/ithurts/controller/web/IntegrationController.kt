package dev.ithurts.controller.web

import dev.ithurts.application.service.internal.sourceprovider.bitbucket.BitbucketDescriptorService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IntegrationController(
    private val bitbucketDescriptorService: BitbucketDescriptorService
) {
    @GetMapping("/connect/bitbucket/descriptor", produces = ["application/json"])
    fun bitbucketDescriptor(): String {
        return bitbucketDescriptorService.getBitbucketConnectDescriptor()
    }
}