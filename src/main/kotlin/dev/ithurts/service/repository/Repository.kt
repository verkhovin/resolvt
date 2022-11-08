package dev.ithurts.service.repository

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "repositories")
data class Repository(
    val name: String,
    val mainBranch: String,
    val workspaceId: String,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!

    fun rename(newName: String): Repository = this.copy(name = newName)
}