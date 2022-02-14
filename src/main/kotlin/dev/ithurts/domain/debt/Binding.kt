package dev.ithurts.domain.debt

import dev.ithurts.application.service.events.Change
import dev.ithurts.application.service.events.ChangeType
import org.bson.types.ObjectId

data class Binding(
    var filePath: String,
    var startLine: Int,
    var endLine: Int,
    var advancedBinding: AdvancedBinding?,
    val id: String = ObjectId().toString()
) {
    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun update(newFilePath: String, coveredCodeHasChanges: Boolean, startLine: Int, endLine: Int): List<Change> {
        val changes = mutableListOf<Change>()
        if (newFilePath != this.filePath) {
            changes.add(Change(id, ChangeType.MOVED, this.filePath, newFilePath))
        }
        if (coveredCodeHasChanges || startLine - endLine != this.startLine - this.endLine) {
            changes.add(Change(id, ChangeType.CODE_CHANGED, null, null))
        }

        this.filePath = newFilePath
        this.startLine = startLine
        this.endLine = if (startLine < endLine) {
            endLine
        } else {
            startLine
        }

        return changes
    }

    fun updateAdvancedManually(path: String, parent: String?, name: String, params: List<String>) {
        val advancedBinding = this.advancedBinding ?: throw IllegalStateException("Binding is not advanced")
        this.filePath = path
        this.advancedBinding = advancedBinding.copy(parent = parent, name = name, params = params)
    }

    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(Binding::class.java)
    }
}