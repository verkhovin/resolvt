package dev.ithurts.application.service.diff.binding.advanced

import dev.ithurts.application.service.diff.BindingSpec
import dev.ithurts.application.service.diff.binding.BindingAdjustmentResult
import dev.ithurts.domain.Language
import dev.ithurts.domain.debt.AdvancedBinding
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.stereotype.Service

@Service
class AdvancedBindingAdjustmentService(
    private val kotlinCodeBasedBindingDefinitionService: KotlinCodeBasedBindingDefinitionService
) {
    fun adjustBinding(
        bindingSpec: BindingSpec,
        advancedBinding: AdvancedBinding,
        fileContent: String,
        diffsByFile: Map<String, List<Diff>>
    ): BindingAdjustmentResult {
        val newBinding = try {
            when (advancedBinding.language) {
                Language.KOTLIN -> {
                    kotlinCodeBasedBindingDefinitionService.getBinding(
                        bindingSpec,
                        advancedBinding,
                        fileContent
                    )
                }
                else -> throw IllegalArgumentException("Language not supported")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not adjust binding", e)
        }
        return BindingAdjustmentResult(
            newBinding,
            emptyList()
        )
    }
}