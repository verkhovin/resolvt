package dev.ithurts.controller.web

import dev.ithurts.application.model.debt.BindingDto
import dev.ithurts.domain.Language
import dev.ithurts.domain.debt.BindingStatus
import org.springframework.stereotype.Service

@Service
class FrontendUtil {
    fun getIconClassBasedOnBinding(binding: BindingDto): String {
        return when {
            binding.status == BindingStatus.TRACKING_LOST -> "bi-exclamation-diamond text-danger"
            binding.isAdvanced() -> getAdvancedBindingIconClass(binding)
            else -> "bi-file-earmark-code"
        }
    }

    private fun getAdvancedBindingIconClass(binding: BindingDto) =
        when (binding.advancedBinding!!.language) {
            Language.JAVA -> "bi-filetype-java"
            else -> "bi-file-earmark-code"
        }
}