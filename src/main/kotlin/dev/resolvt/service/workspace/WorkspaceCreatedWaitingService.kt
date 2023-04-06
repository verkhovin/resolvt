package dev.resolvt.service.workspace

import dev.resolvt.api.rest.github.GithubAuthenticationFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class WorkspaceCreatedWaitingService(private val workspaceRepository: WorkspaceRepository) {
    fun waitUntilWorkspaceIsCreated(accountId: String, acquaintedWorkspaceIds: Set<String>, waitForSeconds: Int) {
        val startTime = System.currentTimeMillis();
        do {
            val workspaceIds = workspaceRepository.getByMembers_accountId(accountId)
                .map { it.id }.toSet()
            if(acquaintedWorkspaceIds != workspaceIds) {
                return
            } else {
                TimeUnit.MILLISECONDS.sleep(150)
            }
            if (System.currentTimeMillis() - startTime > waitForSeconds * 1000) {
                throw WorkspaceCreationTimeoutException("A new workspace was not found in $waitForSeconds " +
                        "seconds for account $accountId. Something went wrong during the app installation.")
            }
        } while (true)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(GithubAuthenticationFilter::class.java)
    }
}