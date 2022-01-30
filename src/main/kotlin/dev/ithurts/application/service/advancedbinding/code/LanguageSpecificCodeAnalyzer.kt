package dev.ithurts.application.service.advancedbinding.code

interface LanguageSpecificCodeAnalyzer {
    fun findCodeEntity(name: String, type: String, fileContent: String): List<CodeEntitySpec>
}