package dev.resolvt.api.rest.bitbucket

import dev.resolvt.configuration.Bitbucket
import dev.resolvt.service.sourceprovider.bitbucket.model.BitbucketAppInstallation
import dev.resolvt.service.sourceprovider.bitbucket.BitbucketWebhookHandler
import dev.resolvt.service.sourceprovider.bitbucket.model.BitbucketWebhookEvent
import dev.resolvt.service.sourceprovider.bitbucket.model.ChangesPushed
import dev.resolvt.service.sourceprovider.bitbucket.model.RepoUpdated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bitbucket")
@CrossOrigin(origins = ["*"])
@Bitbucket
class BitbucketWebhookController(
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