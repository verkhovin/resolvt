package dev.ithurts.domain.debt

import org.bson.types.ObjectId

data class Binding(
    var filePath: String,
    var startLine: Int,
    var endLine: Int,
    var advancedBinding: AdvancedBinding?,
    var active: Boolean = true,
    val id: String = ObjectId().toString()
) {
    init {
        this.endLine = if (startLine < endLine) {
            endLine
        } else {
            startLine
        }
    }

    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun update(newFilePath: String, coveredCodeHasChanges: Boolean, startLine: Int, endLine: Int): List<BindingChange> {
        val bindingChanges = mutableListOf<BindingChange>()
        if (newFilePath != this.filePath) {
            bindingChanges.add(BindingChange(id, ChangeType.MOVED, this.filePath, newFilePath))
        }
        if (coveredCodeHasChanges || startLine - endLine != this.startLine - this.endLine) {
            bindingChanges.add(
                BindingChange(
                    id,
                    ChangeType.CODE_CHANGED,
                    "${this.startLine}:${this.endLine}",
                    "${startLine}:${endLine}"
                )
            )
        }

        this.filePath = newFilePath
        this.startLine = startLine
        this.endLine = if (startLine < endLine) {
            endLine
        } else {
            startLine
        }

        val changeTypes = bindingChanges.map { it.type }.distinct()
        if (changeTypes.size != bindingChanges.size) {
            throw IllegalStateException("Binding change types duplicated: ${this.id}}: ${changeTypes}")
        }
        return bindingChanges
    }

    fun updateAdvanced(path: String, parent: String?, name: String, params: List<String>) {
        val advancedBinding = this.advancedBinding ?: throw IllegalStateException("Binding is not advanced")
        this.filePath = path
        this.advancedBinding = advancedBinding.copy(parent = parent, name = name, params = params)
    }

    fun applyChanges(changes: List<BindingChange>): Binding {
        val newFilePath = changes.last { it.type == ChangeType.MOVED }.to ?: this.filePath
        val newLines = changes.last { it.type == ChangeType.CODE_CHANGED }.to?.split(":")?.map { it.toInt() }
        return this.copy(
            filePath = newFilePath,
            startLine = newLines?.get(0) ?: this.startLine,
            endLine = newLines?.get(0) ?: this.endLine,
        )
    }
}