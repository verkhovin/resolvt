package dev.resolvt.query.model

import dev.resolvt.service.Language
import dev.resolvt.service.debt.model.AdvancedBinding
import dev.resolvt.service.debt.model.Binding
import dev.resolvt.application.utils.jvmSimpleClassName
import dev.resolvt.service.debt.model.BindingStatus

data class BindingDto(
    val id: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val status: BindingStatus?,
    val sourceLink: SourceLink,
    val advancedBinding: AdvancedBindingDto?
) {
    companion object {
        fun from(binding: Binding, sourceLink: SourceLink): BindingDto {
            return BindingDto(
                binding.id,
                binding.filePath,
                binding.startLine,
                binding.endLine,
                binding.status,
                sourceLink,
                binding.advancedBinding?.let { AdvancedBindingDto.from(it) }
            )
        }
    }

    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun type(): String {
        return if (isAdvanced()) {
            advancedBinding!!.type
        } else {
            "Source code"
        }
    }

    fun lines(): String {
        val oneLine = startLine == endLine
        return "line${if (oneLine) "" else "s"} $startLine${if (oneLine) "" else "-$endLine"}"
    }

    fun fullName(): String {
        return if (isAdvanced()) {
            when (advancedBinding!!.language) {
                Language.KOTLIN, Language.JAVA -> jvmFullName(advancedBinding)
                else -> "${advancedBinding.type} ${advancedBinding.name}"
            }
        } else {
            ""
        }
    }

    private fun jvmFullName(advancedBinding: AdvancedBindingDto): String {
        val hasParent = advancedBinding.parent != null
        val hasParams = advancedBinding.params.isNotEmpty()
        return when (advancedBinding.type) {
            "Function", "Method" ->
                " ${if (hasParent) "${jvmSimpleClassName(advancedBinding.parent)}#" else ""}" +
                        advancedBinding.name +
                        (if (hasParams) "(${advancedBinding.params.map(::jvmSimpleClassName).joinToString()})" else "")
            else -> advancedBinding.name
        }
    }
}

data class AdvancedBindingDto(
    val language: Language,
    val type: String,
    val name: String,
    val params: List<String>,
    val parent: String?
) {
    companion object {
        fun from(advancedBinding: AdvancedBinding): AdvancedBindingDto {
            return AdvancedBindingDto(
                advancedBinding.language,
                advancedBinding.type,
                advancedBinding.name,
                advancedBinding.params,
                advancedBinding.parent
            )
        }
    }
}
