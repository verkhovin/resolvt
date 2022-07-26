package dev.ithurts.domain.debt

class BindingChange(
    val bindingId: String,
    val type: ChangeType,
    val from: String?,
    val to: String?
)

enum class ChangeType {
    CODE_CHANGED,
    CODE_MOVED,
    FILE_MOVED,
    ADVANCED_BINDING_TARGET_LOST
}