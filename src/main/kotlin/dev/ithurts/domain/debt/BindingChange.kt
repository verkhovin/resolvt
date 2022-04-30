package dev.ithurts.domain.debt

class BindingChange(
    val bindingId: String,
    val type: ChangeType,
    val from: String?,
    val to: String?
)

enum class ChangeType {
    CODE_CHANGED,
    MOVED,
    ADVANCED_BINDING_TARGET_RENAMED
}