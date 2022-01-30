package dev.ithurts.application.service.code

import dev.ithurts.domain.Language
import org.springframework.stereotype.Service

@Service
class CodeAnalyzer(
    private val kotlinCodeAnalyzer: KotlinCodeAnalyzer
) {
    fun find(
        name: String,
        type: String,
        language: Language,
        fileContent: String,
    ): List<CodeEntitySpec> {
        return try {
            when (language) {
                Language.KOTLIN -> kotlinCodeAnalyzer.findCodeEntity(name, type, fileContent)
                else -> throw IllegalArgumentException("Language not supported")
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Could not adjust binding", e)
        }
    }
}