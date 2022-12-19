package dev.resolvt.service.debt.diff.advancedbinding.code

interface LanguageSpecificCodeAnalyzer {
    fun findCodeEntity(name: String, type: String, fileContent: String): List<CodeEntitySpec>
}