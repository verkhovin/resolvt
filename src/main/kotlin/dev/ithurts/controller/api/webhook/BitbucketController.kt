package dev.ithurts.controller.api.webhook

import dev.ithurts.controller.api.webhook.dto.BitbucketAppInstallation
import dev.ithurts.sourceprovider.bitbucket.BitbucketWebhookHandler
import dev.ithurts.controller.api.webhook.dto.BitbucketWebhookEvent
import dev.ithurts.controller.api.webhook.dto.ChangesPushed
import dev.ithurts.controller.api.webhook.dto.RepoUpdated
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

    @PostMapping("/webhook/repo/push")
    fun repoPushed(@RequestBody changesPushedEvent: BitbucketWebhookEvent<ChangesPushed>) {
        bitbucketWebhookHandler.changesPushed(changesPushedEvent.data)
    }
}