package dev.ithurts.domain.debt

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
        filePath: String,
        startLine: Int,
        endLine: Int,
        updatedAt: Instant
    ) {
        if (status == DebtStatus.PROBABLY_RESOLVED) {
            throw IllegalArgumentException("${DebtStatus.PROBABLY_RESOLVED} can't be set by manual update")
        }
        this.title = title
        this.description = description
        this.status = status
        this.updatedAt = updatedAt
        val binding = this.bindings[0].copy(
            filePath = filePath,
            startLine = startLine,
            endLine = endLine,
        )
        bindings[0] = binding
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

    fun codeDeleted() {
        status = DebtStatus.PROBABLY_RESOLVED
        resolutionReason = ResolutionReason.CODE_DELETED
    }

    fun partlyChanged() {
        status = DebtStatus.PROBABLY_RESOLVED
        resolutionReason = ResolutionReason.PARTLY_CHANGED
    }

    fun manuallyResolved() {
        status = DebtStatus.RESOLVED
        resolutionReason = ResolutionReason.MANUAL
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