package dev.ithurts.application.service.advancedbinding.code


import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import dev.ithurts.application.LineRange
import org.springframework.stereotype.Service

@Service
class JavaCodeAnalyzer: LanguageSpecificCodeAnalyzer {

    override fun findCodeEntity(name: String, type: String, fileContent: String): List<CodeEntitySpec> {
        val compilationUnit = StaticJavaParser.parse(fileContent)
        return when(type.uppercase()) {
            "METHOD" -> {
                findMethod(name, compilationUnit)
            }
            "CLASS" -> {
                findClass(name, compilationUnit)
            }
            else -> throw IllegalArgumentException("Unknown binding type: $type")
        }
    }

    private fun findMethod(name: String, compilationUnit: CompilationUnit): List<CodeEntitySpec> {
        return compilationUnit.findAll(MethodDeclaration::class.java)
            .filter { it.nameAsString == name }
            .map(::buildCodeEntitySpec)
    }

    private fun findClass(name: String, compilationUnit: CompilationUnit): List<CodeEntitySpec> {
        return compilationUnit.findAll(ClassOrInterfaceDeclaration::class.java)
            .filter { it.nameAsString == jvmSimpleClassName(name) }
            .map(::buildCodeEntitySpec)
    }

    private fun buildCodeEntitySpec(
        method: MethodDeclaration
    ) = CodeEntitySpec(
        "Method",
        method.nameAsString,
        method.parameters.map { it.typeAsString },
        buildCodeEntitySpec(method.parentNode.orElse(null) as ClassOrInterfaceDeclaration),
        LineRange(method.range.get().begin.line, method.range.get().end.line)
    )

    private fun buildCodeEntitySpec(javaClass: ClassOrInterfaceDeclaration) = CodeEntitySpec(
        "Class",
        javaClass.fullyQualifiedName.orElseGet(javaClass::getNameAsString),
        emptyList(),
        null,
        LineRange(javaClass.range.get().begin.line, javaClass.range.get().end.line)
    )
}