package dev.resolvt.service.debt.model

import dev.resolvt.service.Language
import org.bson.types.ObjectId


class DebtReport(
    val title: String,
    val description: String,
    val remoteUrl: String,
    val bindings: List<ReportBinding>
)

class ReportBinding(
    val id: String?,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val advancedBinding: ReportAdvancedBinding?,
    val status: BindingStatus
) {
    fun toDomain(): Binding {
        return Binding(
            filePath,
            startLine,
            endLine,
            advancedBinding?.toDomain(),
            status,
            id ?: ObjectId().toString()
        )
    }
}

class ReportAdvancedBinding(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
) {
    fun toDomain(): AdvancedBinding {
        return AdvancedBinding(
            language,
            type,
            name,
            params,
            parent
        )
    }
}