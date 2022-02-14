package dev.ithurts.application.service.advancedbinding

import dev.ithurts.application.LineRange
import dev.ithurts.domain.debt.AdvancedBinding

interface LanguageSpecificBindingService {
    fun lookupBindingLocation(advancedBinding: AdvancedBinding, fileContent: String): LineRange
}