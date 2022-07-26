package dev.ithurts.application.service.internal.diff.advancedbinding

import dev.ithurts.application.model.LineRange
import dev.ithurts.domain.debt.AdvancedBinding

interface LanguageSpecificBindingService {
    fun lookupBindingLocation(advancedBinding: AdvancedBinding, fileContent: String): LineRange?
}