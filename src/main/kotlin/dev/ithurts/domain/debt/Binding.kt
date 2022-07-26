package dev.ithurts.domain.debt

import dev.ithurts.domain.debt.ChangeType.*
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

    fun deriveChanges(
        newFilePath: String,
        coveredCodeHasChanges: Boolean,
        startLine: Int,
        endLine: Int,
    ): List<BindingChange> {
        val bindingChanges = mutableListOf<BindingChange>()
        if (newFilePath != this.filePath) {
            bindingChanges.add(BindingChange(id, FILE_MOVED, this.filePath, newFilePath))
        }

        if (coveredCodeHasChanges || startLine - endLine != this.startLine - this.endLine) {
            bindingChanges.add(
                BindingChange(
                    id,
                    CODE_CHANGED,
                    "${this.startLine}:${this.endLine}",
                    "${startLine}:${endLine}"
                )
            )
        } else if (startLine != this.startLine || endLine != this.endLine) {
            bindingChanges.add(
                BindingChange(
                    id,
                    CODE_MOVED,
                    "${this.startLine}:${this.endLine}",
                    "${startLine}:${endLine}"
                )
            )
        }

        val changeTypes = bindingChanges.map { it.type }.distinct()
        if (changeTypes.size != bindingChanges.size) {
            throw IllegalStateException("Binding change types duplicated: ${this.id}}: ${changeTypes}")
        }
        return bindingChanges
    }

    fun lost(): BindingChange {
        return BindingChange(
            this.id,
            ADVANCED_BINDING_TARGET_LOST,
            null,
            null
        )
    }

    fun applyChanges(changes: List<BindingChange>): Binding {
        val newFilePath = changes.lastOrNull { it.type == FILE_MOVED }?.to ?: this.filePath
        val newLines =
            changes.lastOrNull { it.type == CODE_CHANGED || it.type == CODE_MOVED }?.to?.split(":")?.map { it.toInt() }
        val startLine = newLines?.get(0) ?: this.startLine
        val endLine = newLines?.get(1) ?: this.endLine
        val status = changes.lastOrNull { it.type == ADVANCED_BINDING_TARGET_LOST }?.let { BindingStatus.TRACKING_LOST }
            ?: this.status
        return this.copy(
            filePath = newFilePath,
            startLine = startLine,
            endLine = if (startLine < endLine) endLine else startLine,
            status = status
        )
    }

    fun applyAdvancedBindingManualChange(path: String, parent: String?, name: String, params: List<String>): Binding {
        val advancedBinding = this.advancedBinding ?: throw IllegalStateException("Binding is not advanced")
        return this.copy(
            filePath = path,
            advancedBinding = advancedBinding.copy(parent = parent, name = name, params = params)
        )
    }

    fun archive() = this.copy(status = BindingStatus.ARCHIVED)
}

enum class BindingStatus {
    ACTIVE, ARCHIVED, TRACKING_LOST
}