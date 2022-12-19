package dev.resolvt.api.rest.github

import dev.resolvt.configuration.Github
import dev.resolvt.service.sourceprovider.github.GithubWebhookHandler
import dev.resolvt.service.sourceprovider.github.model.GithubWebhookEvent
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("github")
@CrossOrigin(origins = ["*"])
@Github
class GithubWebhookController(
    private val githubWebhookHandler: GithubWebhookHandler
) {
    @PostMapping("/webhook")
    fun listen(@RequestBody webhookEvent: GithubWebhookEvent) {
        when(webhookEvent.action) {
            "created" -> {
                githubWebhookHandler.appInstalled(webhookEvent)
                return
            }
        }
        if (webhookEvent.before != null && webhookEvent.after != null) {
            githubWebhookHandler.changesPushed(webhookEvent)
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(GithubWebhookController::class.java)
    }
}