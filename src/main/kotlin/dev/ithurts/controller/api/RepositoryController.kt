package dev.ithurts.controller.api

import dev.ithurts.application.model.RepositoryDto
import dev.ithurts.application.query.RepositoryQueryRepository
import dev.ithurts.application.service.internal.RepositoryInfoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/repositories")
class RepositoryController(
    private val repositoryInfoService: RepositoryInfoService,
    private val repositoryQueryRepository: RepositoryQueryRepository
) {
    @GetMapping
    fun getRepository(@RequestParam remoteUrl: String): RepositoryDto {
        val repositoryInfo = repositoryInfoService.parseRemoteUrl(remoteUrl)
        return repositoryQueryRepository.getRepository(repositoryInfo)
    }
}