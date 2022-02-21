package dev.ithurts.application.internal.advancedbinding.code

import dev.ithurts.application.model.LineRange
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstNode
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.springframework.stereotype.Service

@Service
class KotlinCodeAnalyzer : LanguageSpecificCodeAnalyzer {
    override fun findCodeEntity(name: String, type: String, fileContent: String): List<CodeEntitySpec> {
        val source = AstSource.String("", fileContent)
        val kotlinFile = KotlinGrammarAntlrKotlinParser.parseKotlinFile(source)
        val summary = kotlinFile.summary(false)
        when (type) {
            "Function" -> {
                //FIXME oh well, this one is going to work only if all this kotlinx.ast parsing staff is synchronous/single-threaded
                val functions = mutableListOf<CodeEntitySpec>()
                summary.onSuccess { asts ->
                    asts.forEach { ast ->
                        findNode(ast, null, "fun", name) { node, parent ->
                            functions.add(
                                buildCodeSpec(
                                    node,
                                    parent?.let { buildCodeSpec(it, null) }
                                )
                            )
                        }
                    }
                }
                return functions
            }
            "Class" -> {
                val classes = mutableListOf<CodeEntitySpec>()
                summary.onSuccess { asts ->
                    asts.forEach { ast ->
                        findNode(ast, null, "class", name) { node, parent ->
                            classes.add(
                                buildCodeSpec(
                                    node,
                                    null
                                )
                            )
                        }
                    }
                }
                return classes
            }
            else -> throw IllegalArgumentException("Unknown binding type: ${type}")
        }
    }

    private fun buildCodeSpec(node: KlassDeclaration, parent: CodeEntitySpec?) = CodeEntitySpec(
        KEYWORD_TO_TYPE[node.keyword]!!,
        node.identifier?.rawName ?: "",
        node.parameter.map { param -> param.type.first().rawName },
        parent,
        LineRange(node.info!!.start.line, node.info!!.stop.line)
    )

    private fun findNode(
        ast: Ast,
        parent: KlassDeclaration?,
        expectedType: String,
        expectedName: String,
        callback: (KlassDeclaration, KlassDeclaration?) -> Unit
    ) {
        when (ast) {
            is KlassDeclaration -> {
                if (ast.keyword == expectedType && ast.identifier?.rawName == expectedName) {
                    callback(ast, parent)
                }
                for (child in ast.children) {
                    findNode(child, ast, expectedType, expectedName, callback)
                }
            }
            is AstNode -> {
                for (child in ast.children) {
                    findNode(child, parent, expectedType, expectedName, callback)
                }
            }
        }
    }

    companion object {
        val KEYWORD_TO_TYPE = mapOf(
            "fun" to "Function",
            "class" to "Class",
            "interface" to "Interface",
            "enum" to "Enum",
            "val" to "Property",
            "var" to "Property",
        )
    }
}