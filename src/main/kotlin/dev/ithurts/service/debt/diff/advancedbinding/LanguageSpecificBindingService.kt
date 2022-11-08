package dev.ithurts.service.debt.diff.advancedbinding

import dev.ithurts.application.model.LineRange
import dev.ithurts.service.debt.model.AdvancedBinding

interface LanguageSpecificBindingService {
    fun lookupBindingLocation(advancedBinding: AdvancedBinding, fileContent: String): LineRange?
}