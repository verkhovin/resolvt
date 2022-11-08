package dev.ithurts.api.rest

import dev.ithurts.query.model.RepositoryDto
import dev.ithurts.query.RepositoryQueryRepository
import dev.ithurts.service.repository.RepositoryInfoService
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