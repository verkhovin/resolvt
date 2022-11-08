package dev.ithurts.service.debt.debtevent

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "debt-events")
data class DebtEvent(
    val debtId: String,
    val repositoryId: String,
    val commitHash: String,
    val changes: List<BindingChange>,
    @CreatedDate
    val createdAt: Instant? = null,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!
}

class BindingChange(
    val bindingId: String,
    val type: ChangeType,
    val from: String?,
    val to: String?,
    val visible: Boolean
)

enum class ChangeType {
    CODE_CHANGED,
    CODE_MOVED,
    FILE_MOVED,
    ADVANCED_BINDING_TARGET_LOST
}
