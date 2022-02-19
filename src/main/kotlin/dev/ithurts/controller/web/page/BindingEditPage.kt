package dev.ithurts.controller.web.page

import dev.ithurts.application.model.debt.BindingDto
import dev.ithurts.application.model.debt.DebtDto

data class BindingEditPage(
    val debt: DebtDto,
    val binding: BindingDto,
) {
    val bindingForm = BindingEditForm(
        binding.filePath,
        "${binding.startLine}:${binding.endLine}"
    )
    val advancedBindingEditForm = if(binding.isAdvanced()) {
        AdvancedBindingEditForm(
            binding.filePath,
            binding.advancedBinding!!.parent,
            binding.advancedBinding.name,
            binding.advancedBinding.params.joinToString { it }
        )
    } else null
}

data class BindingEditForm(
    val path: String,
    val linespec: String?
)

data class AdvancedBindingEditForm(
    val path: String,
    val parent: String?,
    val name: String?,
    val params: String?
)