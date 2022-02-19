package dev.ithurts.application.internal.advancedbinding.code

interface LanguageSpecificCodeAnalyzer {
    fun findCodeEntity(name: String, type: String, fileContent: String): List<CodeEntitySpec>
}