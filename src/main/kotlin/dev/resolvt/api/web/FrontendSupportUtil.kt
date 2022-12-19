package dev.resolvt.api.web

import dev.resolvt.service.Language
import dev.resolvt.query.model.BindingDto
import dev.resolvt.service.debt.model.BindingStatus
import org.springframework.core.env.Environment
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