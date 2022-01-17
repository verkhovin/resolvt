package dev.ithurts.domain.debt

import dev.ithurts.domain.DomainEntity
import javax.persistence.*

@Entity
class Debt(
    val title: String,
    @Column(columnDefinition = "TEXT")
    val description: String,
    @Enumerated(EnumType.STRING)
    var status: DebtStatus,
    val filePath: String,
    var startLine: Int,
    var endLine: Int,
    val votes: Int,
    val creatorAccountId: Long,
    val repositoryId: Long,
    var resolutionReason: ResolutionReason? = null,

): DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val _id: Long? = null

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