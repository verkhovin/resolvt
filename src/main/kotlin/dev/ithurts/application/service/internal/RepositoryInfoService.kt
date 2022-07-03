package dev.ithurts.application.service.internal

import dev.ithurts.application.model.RepositoryInfo
import dev.ithurts.domain.SourceProvider
import dev.ithurts.application.exception.DebtReportFailedException
import org.springframework.stereotype.Service


@Service
class RepositoryInfoService {
    fun parseRemoteUrl(remoteUrl: String): RepositoryInfo {
        val matchResult =
            REMOTE_URL_REGEX.matchEntire(remoteUrl) ?: throw DebtReportFailedException("Failed to parse remote url")
        val name = matchResult.groups["name"]?.value
            ?: throw DebtReportFailedException("Failed to parse repo name out of remote url")
        val organisationName = matchResult.groups["organisation"]?.value
            ?: throw DebtReportFailedException("Failed to parse organisation name out of remote url")
        return RepositoryInfo(name, organisationName, SourceProvider.BITBUCKET)
    }

    companion object {
        private val REMOTE_URL_REGEX =
            "(?<host>(git@|https://)([\\w.@]+)([/:]))(?<organisation>[\\w,\\-_]+)/(?<name>[\\w,\\-_]+)(.git)?((/)?)".toRegex()
    }
}