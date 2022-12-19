package dev.resolvt.service.debt.diff.advancedbinding

import dev.resolvt.application.model.LineRange
import dev.resolvt.service.debt.model.AdvancedBinding

interface LanguageSpecificBindingService {
    fun lookupBindingLocation(advancedBinding: AdvancedBinding, fileContent: String): LineRange?
}