package dev.ithurts.domain.debtevent

import dev.ithurts.domain.debt.ChangeType
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "debt-events")
data class DebtEvent(
    val debtId: String,
    val repositoryId: String,
    val commitHash: String,
    val changes: List<BindingChange>,
    val createdAt: Instant,
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
