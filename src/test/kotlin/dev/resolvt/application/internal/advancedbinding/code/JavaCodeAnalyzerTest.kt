package dev.resolvt.application.internal.advancedbinding.code

import dev.resolvt.application.model.LineRange
import dev.resolvt.service.debt.diff.advancedbinding.code.CodeEntitySpec
import dev.resolvt.service.debt.diff.advancedbinding.code.JavaCodeAnalyzer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JavaCodeAnalyzerTest {
    private val codeAnalyzer = JavaCodeAnalyzer()

    @Test
    fun `method is mapped correctly to code entity spec`() {
        val methodSpecs = codeAnalyzer.findCodeEntity("doSomething", "Method", CODE)
        assertEquals(METHOD_SPEC, methodSpecs[0])
    }

    @Test
    fun `class is mapped correctly to code entity spec`() {
        val classSpecs = codeAnalyzer.findCodeEntity("TestClass", "Class", CODE)
        assertEquals(CLASS_SPEC, classSpecs[0])
    }

    companion object {
        val CLASS_SPEC = CodeEntitySpec(
            "Class",
            "dev.resolvt.application.service.advancedbinding.code.TestClass",
            emptyList(),
            null,
            LineRange(5, 10)
        )

        val METHOD_SPEC = CodeEntitySpec(
            "Method", "doSomething", listOf("String", "Long"),
            CLASS_SPEC,
            LineRange(7, 9)
        )
        val CODE = """
            package dev.resolvt.application.service.advancedbinding.code;
            
            import org.springframework.stereotype.Service;
            
            @Service
            public class TestClass {
                public void doSomething(String str, Long l) {
                    System.out.println("Hello World!");
                }
            }
            """.trimIndent()
    }
}