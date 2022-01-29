package dev.ithurts.domain.debt

import dev.ithurts.domain.DomainEntity
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.persistence.*

@Entity
data class Binding(
    var filePath: String,
    var startLine: Int,
    var endLine: Int,
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true, optional = true)
    @Fetch(FetchMode.JOIN)
    val advancedBinding: AdvancedBinding?
) : DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override val id: Long? = null

    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun update(filePath: String, startLine: Int, endLine: Int) {
        this.filePath = filePath
        this.startLine = startLine
        this.endLine = if (startLine < endLine) {
            endLine
        } else {
            log.error("startLine < endLine for binding $id")
            startLine
        }
    }

    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(Binding::class.java)
    }
}