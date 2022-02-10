package dev.ithurts.application.dto.debt

import dev.ithurts.domain.Language
import dev.ithurts.domain.debt.AdvancedBinding
import dev.ithurts.domain.debt.Binding
import dev.ithurts.utils.simpleJvmClassName

data class BindingDto(
    val id: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
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
                " ${if (hasParent) "${simpleJvmClassName(advancedBinding.parent)}#" else ""}" +
                        advancedBinding.name +
                        (if (hasParams) "(${advancedBinding.params.map(::simpleJvmClassName).joinToString()})" else "")
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
