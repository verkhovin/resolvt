package dev.ithurts.model.debt

import dev.ithurts.model.Account
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
    @ManyToOne
    @JoinColumn(name = "account_id")
    val account: Account,
    @ManyToOne
    @JoinColumn(name = "organisation_id")
    val repository: Repository,
    var resolutionReason: ResolutionReason? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) {
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