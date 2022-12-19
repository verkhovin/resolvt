package dev.resolvt.api.rest

import dev.resolvt.query.model.RepositoryDto
import dev.resolvt.query.RepositoryQueryRepository
import dev.resolvt.service.repository.RepositoryInfoService
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