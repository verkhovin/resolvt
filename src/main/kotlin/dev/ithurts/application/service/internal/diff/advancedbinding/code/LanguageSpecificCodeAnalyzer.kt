package dev.ithurts.application.service.internal.diff.advancedbinding.code

interface LanguageSpecificCodeAnalyzer {
    fun findCodeEntity(name: String, type: String, fileContent: String): List<CodeEntitySpec>
}