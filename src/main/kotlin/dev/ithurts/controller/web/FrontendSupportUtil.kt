package dev.ithurts.controller.web

import dev.ithurts.application.model.debt.BindingDto
import dev.ithurts.domain.Language
import org.springframework.stereotype.Service

@Service
class FrontendUtil {
    fun getIconClassBasedOnBinding(binding: BindingDto): String {
        return if (binding.isAdvanced()) {
            when(binding.advancedBinding!!.language) {
                Language.JAVA -> "bi-filetype-java"
                else -> "bi-file-earmark-code"
            }
        } else {
            "bi-file-earmark-code"
        }
    }
}