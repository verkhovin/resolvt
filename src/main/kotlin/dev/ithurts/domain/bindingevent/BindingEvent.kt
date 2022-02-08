package dev.ithurts.domain.bindingevent

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "binding-events")
data class BindingEvent(
    val debtId: String,
    val bindingId: String,
    val repositoryId: String,
    val commitHash: String,
    val changes: List<Change>,
    val createdAt: Instant,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!
}

class Change(
    val type: ChangeType,
    val from: String?,
    val to: String?
)

enum class ChangeType {
    CODE_CHANGED,
    MOVED,
    ADVANCED_BINDING_TARGET_RENAMED
}
