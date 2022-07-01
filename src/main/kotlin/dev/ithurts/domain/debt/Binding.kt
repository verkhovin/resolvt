package dev.ithurts.domain.debt

import org.bson.types.ObjectId

data class Binding(
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val advancedBinding: AdvancedBinding?,
    val active: Boolean = true,
    val id: String = ObjectId().toString(),
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

        val changeTypes = bindingChanges.map { it.type }.distinct()
        if (changeTypes.size != bindingChanges.size) {
            throw IllegalStateException("Binding change types duplicated: ${this.id}}: ${changeTypes}")
        }
        return bindingChanges
    }

    fun applyChanges(changes: List<BindingChange>): Binding {
        val newFilePath = changes.lastOrNull { it.type == ChangeType.MOVED }?.to ?: this.filePath
        val newLines = changes.lastOrNull { it.type == ChangeType.CODE_CHANGED }?.to?.split(":")?.map { it.toInt() }
        val startLine = newLines?.get(0) ?: this.startLine
        val endLine = newLines?.get(0) ?: this.endLine
        return this.copy(
            filePath = newFilePath,
            startLine = startLine,
            endLine = if (startLine < endLine) endLine else startLine
        )
    }

    fun applyAdvancedBindingManualChange(path: String, parent: String?, name: String, params: List<String>): Binding {
        val advancedBinding = this.advancedBinding ?: throw IllegalStateException("Binding is not advanced")
        return this.copy(
            filePath = path,
            advancedBinding = advancedBinding.copy(parent = parent, name = name, params = params)
        )
    }

    fun inactivate() = this.copy(active = false)
}