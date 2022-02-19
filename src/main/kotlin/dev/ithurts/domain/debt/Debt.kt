package dev.ithurts.domain.debt

import dev.ithurts.application.events.Change
import dev.ithurts.application.events.ChangeType
import dev.ithurts.application.events.DebtBindingChangedEvent
import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "debts")
data class Debt(
    var title: String,
    var description: String,
    var status: DebtStatus,
    val creatorAccountId: String,
    val repositoryId: String,
    val workspaceId: String,
    val bindings: MutableList<Binding>,
    val createdAt: Instant,
    var updatedAt: Instant = createdAt,
    val votes: MutableList<DebtVote> = mutableListOf(),
    var resolutionReason: ResolutionReason? = null,
    @BsonId
    val _id: String? = null
) {
    val id: String
        get() = _id!!

    fun update(
        title: String,
        description: String,
        status: DebtStatus,
        updatedAt: Instant
    ) {
        if (status == DebtStatus.PROBABLY_RESOLVED) {
            throw IllegalArgumentException("${DebtStatus.PROBABLY_RESOLVED} can't be set by manual update")
        }
        this.title = title
        this.description = description
        this.status = status
        this.updatedAt = updatedAt
    }

    fun vote(accountId: String) {
        val vote = DebtVote(accountId)
        if (!votes.contains(vote)) {
            votes.add(vote)
        }
    }

    fun downVote(accountId: String) {
        val vote = DebtVote(accountId)
        votes.remove(vote)
    }

    fun accountVoted(accountId: String) = this.votes.contains(DebtVote(accountId))

    fun eventForChanges(changes: List<Change>, commitHash: String): DebtBindingChangedEvent {
        val movedBindingIds = changes.filter { it.type == ChangeType.MOVED }.map { it.bindingId }
        val acceptedChanges = if (movedBindingIds == this.bindings.map { it.id }) {
            changes.filter { it.type != ChangeType.MOVED }
        } else {
            changes
        }
        return DebtBindingChangedEvent(
            this,
            id,
            repositoryId,
            commitHash,
            acceptedChanges
        )
    }
}

enum class DebtStatus {
    OPEN,
    PROBABLY_RESOLVED,
    RESOLVED
}

enum class ResolutionReason {
    CODE_DELETED,
    PARTLY_CHANGED,
    MANUAL
}