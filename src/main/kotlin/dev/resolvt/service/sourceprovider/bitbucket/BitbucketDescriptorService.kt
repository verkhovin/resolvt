package dev.resolvt.service.sourceprovider.bitbucket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.resolvt.configuration.ApplicationProperties
import dev.resolvt.configuration.Bitbucket
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

@Service
@Bitbucket
class BitbucketDescriptorService(
    private val resourceLoader: ResourceLoader,
    private val applicationProperties: ApplicationProperties,
    private val objectMapper: ObjectMapper,
) {
    fun getBitbucketConnectDescriptor(): String {
        val descriptorTemplate: MutableMap<String, Any?> = resourceLoader
            .getResource(ResourceLoader.CLASSPATH_URL_PREFIX + "bitbucket-descriptor.json")
            .inputStream.use { objectMapper.readValue(it) }
        descriptorTemplate["key"] = applicationProperties.bitbucket!!.appName
        descriptorTemplate["name"] = "Resolvt for Bitbucket [${applicationProperties.bitbucket.appName}]"
        descriptorTemplate["baseUrl"] = applicationProperties.baseUrl
        return objectMapper.writeValueAsString(descriptorTemplate)
    }
}