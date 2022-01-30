package dev.ithurts.application.service.advancedbinding

import dev.ithurts.application.LineRange
import dev.ithurts.application.service.advancedbinding.code.KotlinCodeAnalyzer
import dev.ithurts.domain.debt.AdvancedBinding
import org.springframework.stereotype.Service

@Service
class KotlinBindingService(
    private val codeAnalyzer: KotlinCodeAnalyzer
) : LanguageSpecificBindingService {
    override fun lookupBindingLocation(
        advancedBinding: AdvancedBinding,
        fileContent: String
    ): LineRange {
        val matchedCodeEntities = codeAnalyzer.findCodeEntity(advancedBinding.name, advancedBinding.type, fileContent)
        val entity = matchedCodeEntities.asSequence()
            // FIXME actually, class could be renamed. in this case binding will be lost. System of assumptions could help here.
            .filter { it.parent?.name == simpleClassName(advancedBinding.parent) }
            // Well, the number of parameters or their type could change. If we know that the function hasn't had overrides,
            // we can quite safely assume that the parameters were changed, and we didn't found some override here
            .first { function ->
                function.parameters == advancedBinding.params.map(::simpleClassName)
            }
        return LineRange(entity.lines.start, entity.lines.end)
    }

    private fun simpleClassName(name: String?): String? {
        return name?.substringAfterLast(".")
    }
}