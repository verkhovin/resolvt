package dev.ithurts.domain.debt

import dev.ithurts.domain.DomainEntity
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.persistence.*

@Entity
class Debt(
    var title: String,
    @Column(columnDefinition = "TEXT")
    var description: String,
    @Enumerated(EnumType.STRING)
    var status: DebtStatus,
    val creatorAccountId: Long,
    val repositoryId: Long,
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "debt_id")
    val bindings: MutableList<Binding>,
    @Enumerated(EnumType.STRING)
    var resolutionReason: ResolutionReason? = null
) : DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    val votes: MutableList<DebtVote> = mutableListOf()

    /// TODO REMOVE
    var startLine: Int
        get() = bindings.first().startLine
        set(value) {
            bindings.first().startLine = value
        }
    var endLine: Int
        get() = bindings.first().endLine
        set(value) {
            bindings.first().endLine = value
        }
    var filePath: String
        get() = bindings.first().filePath
        set(value) {
            bindings.first().filePath = value
        }
    ///

    fun update(
        title: String,
        description: String,
        status: DebtStatus,
        filePath: String,
        startLine: Int,
        endLine: Int
    ) {
        if (status == DebtStatus.PROBABLY_RESOLVED) {
            throw IllegalArgumentException("${DebtStatus.PROBABLY_RESOLVED} can't be set by manual update")
        }
        this.title = title
        this.description = description
        this.status = status
        val binding = this.bindings[0].copy(
            filePath = filePath,
            startLine = startLine,
            endLine = endLine,
        )
        bindings[0] = binding
    }

    fun updateBinding(id: Long, filePath: String, startLine: Int, endLine: Int) {
        val binding = bindings.find { it.id == id } ?: return
        val newBinding = binding.copy(
            filePath = filePath,
            startLine = startLine,
            endLine = endLine,
        )
        bindings[bindings.indexOf(binding)] = newBinding
    }

    fun vote(accountId: Long) {
        val vote = DebtVote(accountId)
        if (!votes.contains(vote)) {
            votes.add(vote)
        }
    }

    fun downVote(accountId: Long) {
        val vote = DebtVote(accountId)
        votes.remove(vote)
    }

    fun accountVoted(accountId: Long) = this.votes.contains(DebtVote(accountId))

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