package dev.ithurts.domain.debt

import dev.ithurts.domain.DomainEntity
import dev.ithurts.domain.Language
import dev.ithurts.external.StringListConverter
import javax.persistence.*

//Fake entity
@Entity
data class AdvancedBinding(
    @Enumerated(EnumType.STRING)
    val language: Language,
    val type: String,
    val name: String,
    @Convert(converter = StringListConverter::class)
    val params: List<String>,
    val parent: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
}