package dev.ithurts.api.web

import dev.ithurts.service.Language
import dev.ithurts.query.model.BindingDto
import dev.ithurts.service.debt.model.BindingStatus
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