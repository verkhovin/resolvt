package dev.ithurts.application.service.diff.binding.advanced

import dev.ithurts.application.service.diff.BindingSpec
import dev.ithurts.domain.debt.AdvancedBinding
import kotlinx.ast.common.AstSource
import kotlinx.ast.common.ast.Ast
import kotlinx.ast.common.ast.AstNode
import kotlinx.ast.common.klass.KlassDeclaration
import kotlinx.ast.grammar.kotlin.common.summary
import kotlinx.ast.grammar.kotlin.target.antlr.kotlin.KotlinGrammarAntlrKotlinParser
import org.springframework.stereotype.Service

@Service
class KotlinCodeBasedBindingDefinitionService : CodeBasedBindingDefinitionService {
    override fun getBinding(bindingSpec: BindingSpec, advancedBinding: AdvancedBinding, code: String): BindingSpec {
        val source = AstSource.String("", code)
        val kotlinFile = KotlinGrammarAntlrKotlinParser.parseKotlinFile(source)
        when (advancedBinding.type) {
            "Function" -> {
                //FIXME oh well, this one is going to work only if all this kotlinx.ast parsing staff is synchronous/single-threaded
                val functions = mutableListOf<Pair<KlassDeclaration, String?>>()
                kotlinFile.summary(false).onSuccess {
                    it.forEach { ast ->
                        findNode(ast, null, "fun", advancedBinding.name) { node, parent ->
                            functions.add(node to parent)
                        }
                    }
                }
                val function = functions.asSequence()
                    .filter { it.second == simpleClassName(advancedBinding.parent) }
                    .map { it.first }
                    .filter { function ->
                        function.parameter.map { param -> param.type.first().rawName } == advancedBinding.params.map(::simpleClassName)
                    }.first()
                return BindingSpec(
                    bindingSpec.filePath,
                    function.info!!.start.line,
                    function.info!!.stop.line
                )
            }
            else -> throw IllegalArgumentException("Unknown binding type: ${advancedBinding.type}")
        }
    }

    private fun findNode(
        ast: Ast,
        parentNodeName: String?,
        expectedType: String,
        expectedName: String,
        callback: (KlassDeclaration, String?) -> Unit
    ) {
        when (ast) {
            is KlassDeclaration -> {
                if (ast.keyword == expectedType && ast.identifier?.rawName == expectedName) {
                    callback(ast, parentNodeName)
                }
                for (child in ast.children) {
                    findNode(child, ast.identifier?.rawName, expectedType, expectedName, callback)
                }
            }
            is AstNode -> {
                for (child in ast.children) {
                    findNode(child, parentNodeName, expectedType, expectedName, callback)
                }
            }
        }
    }

    private fun simpleClassName(name: String?): String? {
        return name?.substringAfterLast(".")
    }

    companion object {
//        private val BINDING_TYPE_TO_KEYWORD = mapOf(
//            "Function" to listOf("fun"),
//            "Class" to listOf("class"),
//            "Field" to listOf("val", "var"),
//        )
    }
}