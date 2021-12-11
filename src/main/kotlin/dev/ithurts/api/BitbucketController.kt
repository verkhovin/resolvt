package dev.ithurts.api

import dev.ithurts.model.api.bitbucket.BitbucketAppInstallation
import dev.ithurts.sourceprovider.bitbucket.BitbucketWebhookHandler
import dev.ithurts.sourceprovider.bitbucket.dto.webhook.BitbucketWebhookEvent
import dev.ithurts.sourceprovider.bitbucket.dto.webhook.RepoUpdated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bitbucket")
@CrossOrigin(origins = ["*"])
class BitbucketController(
    private val bitbucketWebhookHandler: BitbucketWebhookHandler
) {
    @PostMapping("/installed")
    fun installed(@RequestBody bitbucketAppInstallation: BitbucketAppInstallation) {
        bitbucketWebhookHandler.appInstalled(bitbucketAppInstallation)
    }

    @PostMapping("/uninstalled")
    fun uninstalled(@RequestBody bitbucketAppInstallation: BitbucketAppInstallation) {
        println(bitbucketAppInstallation)
        bitbucketWebhookHandler.appUninstalled(bitbucketAppInstallation)
    }

    @PostMapping("/webhook/repo/updated")
    fun repoUpdated(@RequestBody repoUpdatedEvent: BitbucketWebhookEvent<RepoUpdated>) {
        bitbucketWebhookHandler.repoUpdated(repoUpdatedEvent.data)
    }
}