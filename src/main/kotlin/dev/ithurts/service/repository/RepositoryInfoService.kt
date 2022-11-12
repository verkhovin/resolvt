package dev.ithurts.service.repository

import dev.ithurts.service.SourceProvider
import org.springframework.stereotype.Service


@Service
class RepositoryInfoService {
    fun parseRemoteUrl(remoteUrl: String): RepositoryInfo {
        val matchResult =
            REMOTE_URL_REGEX.matchEntire(remoteUrl) ?: throw DebtReportFailedException("Failed to parse remote url")
        val host = matchResult.groups["host"]?.value
            ?: throw DebtReportFailedException("Failed to parse repo host out of remote url")
        val name = matchResult.groups["name"]?.value
            ?: throw DebtReportFailedException("Failed to parse repo name out of remote url")
        val organisationName = matchResult.groups["organisation"]?.value
            ?: throw DebtReportFailedException("Failed to parse organisation name out of remote url")
        val sourceProvider = when {
            host.contains("bitbucket") -> SourceProvider.BITBUCKET
            host.contains("github") -> SourceProvider.GITHUB
            else -> throw DebtReportFailedException("Failed to recognize source provider $host")
        }
        return RepositoryInfo(name, organisationName, sourceProvider)
    }

    companion object {
        private val REMOTE_URL_REGEX =
            "(?<host>(git@|https://)([\\w.@]+)([/:]))(?<organisation>[\\w,\\-_]+)/(?<name>[\\w,\\-_]+)(.git)?((/)?)".toRegex()
    }
}