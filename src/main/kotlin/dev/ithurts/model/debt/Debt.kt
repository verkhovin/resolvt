package dev.ithurts.model.debt

import dev.ithurts.model.Account
import javax.persistence.*

@Entity
class Debt(
    val title: String,
    val description: String,
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)

enum class DebtStatus {
    OPEN,
    RESOLVED,
    PROBABLY_RESOLVED_CODE_DELETED,
    PROBABLY_RESOLVED_PARTLY_CHANGED,
}
