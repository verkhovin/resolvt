package dev.ithurts.application.internal

import dev.ithurts.application.model.LineRange
import dev.ithurts.application.service.internal.diff.git.GitDiffAnalyzer
import dev.ithurts.application.service.internal.diff.git.HunkResolvingStrategy
import io.reflectoring.diffparser.api.UnifiedDiffParser
import io.reflectoring.diffparser.api.model.Diff
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.kotlin.anyOrNull


class GitDiffAnalyzerTest {
    val hunkResolvingStrategy = mock(HunkResolvingStrategy::class.java)
    val gitDiffAnalyzer = GitDiffAnalyzer(hunkResolvingStrategy)


    @Test
    fun `when selection is located before the diff it shouldn't be changed`() {
        val result = gitDiffAnalyzer.lookupCodeRangeChange(LineRange(5, 10), DIFF_6DELETE_1ADD).position
        assertEquals(LineRange(5, 10), result)
        then(hunkResolvingStrategy).should(never()).processHunk(anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `when selection is located after the diff its position should be adjusted`() {
        val result = gitDiffAnalyzer.lookupCodeRangeChange(LineRange(36, 45), DIFF_6DELETE_1ADD).position
        assertEquals(LineRange(31, 40), result)
        then(hunkResolvingStrategy).should(never()).processHunk(anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `when selection is around the hunk the end of debt should be adjusted and selection considered changed`() {
        val result = gitDiffAnalyzer.lookupCodeRangeChange(LineRange(5, 36), DIFF_6DELETE_1ADD)
        assertEquals(LineRange(5, 31), result.position)
        assertEquals(true, result.wasSelectedCodeChanged)
        then(hunkResolvingStrategy).should(never()).processHunk(anyOrNull(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `when debt start is inside of the hunk processing should be delegated to HunkProcessingStrategy`() {
        gitDiffAnalyzer.lookupCodeRangeChange(LineRange(35, 40), DIFF_6DELETE_1ADD)
        then(hunkResolvingStrategy).should(times(1)).processHunk(anyOrNull(), anyOrNull(), anyOrNull())
    }


    companion object {
        val DIFF_6DELETE_1ADD: MutableList<Diff> = UnifiedDiffParser().parse(
            """
Index: src/main/java/ru/verkhovin/poker/model/Room.java
IDEA additional info:
Subsystem: com.intellij.openapi.diff.impl.patch.CharsetEP
<+>UTF-8
===================================================================
diff --git a/src/main/java/ru/verkhovin/poker/model/Room.java b/src/main/java/ru/verkhovin/poker/model/Room.java
--- a/src/main/java/ru/verkhovin/poker/model/Room.java	(revision 6302d6c4dc4771e3657d3322c70b8f7633409755)
+++ b/src/main/java/ru/verkhovin/poker/model/Room.java	(date 1639737736752)
@@ -24,12 +24,7 @@
   //TODO store as jsonb or elementcollections. Converter now is called like 4 times per request which is too much
   @Convert(converter = EstimateConverter.class)
   private List<Estimate> estimates;
-
-
-  public Room() {
-    this.showEstimates = false;
-    this.estimates = new ArrayList<>();
-  }
+  public Room() {this.showEstimates = false;this.estimates = new ArrayList<>();}

   public Long getId() {
     return id;

        """.toByteArray()
        )
    }
}