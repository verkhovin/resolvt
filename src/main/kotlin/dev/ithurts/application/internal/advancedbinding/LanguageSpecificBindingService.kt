package dev.ithurts.application.internal.advancedbinding

import dev.ithurts.application.model.LineRange
import dev.ithurts.domain.debt.AdvancedBinding

interface LanguageSpecificBindingService {
    fun lookupBindingLocation(advancedBinding: AdvancedBinding, fileContent: String): LineRange
}