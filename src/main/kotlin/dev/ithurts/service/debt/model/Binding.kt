package dev.ithurts.service.debt.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

data class Binding(
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val advancedBinding: AdvancedBinding?,
    val status: BindingStatus,
    val id: String = ObjectId().toString(),
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun update(filePath: String = this.filePath, startLine: Int = this.startLine, endLine: Int = this.endLine): Binding {
        return this.copy(
            filePath = filePath,
            startLine = startLine,
            endLine = endLine
        )
    }

    fun updateAdvancedBinding(parent: String?, name: String, params: List<String>): Binding {
        val advancedBinding = this.advancedBinding ?: throw IllegalStateException("Binding is not advanced")
        return this.copy(
            advancedBinding = advancedBinding.copy(parent = parent, name = name, params = params)
        )
    }

    fun archive() = this.copy(status = BindingStatus.ARCHIVED)
}

enum class BindingStatus {
    ACTIVE, ARCHIVED, TRACKING_LOST
}