package dev.ithurts.domain.debt

import dev.ithurts.application.service.events.Change
import dev.ithurts.application.service.events.ChangeType
import org.bson.types.ObjectId

data class Binding(
    var filePath: String,
    var startLine: Int,
    var endLine: Int,
    val advancedBinding: AdvancedBinding?,
    val id: String = ObjectId().toString()
) {
    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun update(newFilePath: String, coveredCodeHasChanges: Boolean, startLine: Int, endLine: Int): List<Change> {
        val changes = mutableListOf<Change>()
        if (newFilePath != this.filePath) {
            changes.add(Change(ChangeType.MOVED, this.filePath, newFilePath))
        }
        if (coveredCodeHasChanges || startLine != this.startLine || endLine != this.endLine) {
            changes.add(Change(ChangeType.CODE_CHANGED, null, null))
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

    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(Binding::class.java)
    }
}