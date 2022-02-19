package dev.ithurts.application.internal

import dev.ithurts.application.internal.git.Direction
import io.reflectoring.diffparser.api.UnifiedDiffParser
import org.junit.jupiter.api.Test
import dev.ithurts.application.internal.git.HunkResolvingStrategy
import dev.ithurts.application.internal.git.LineRangeMutator
import org.junit.jupiter.api.Assertions.*

class HunkResolvingStrategyTest {
    private val strategy = HunkResolvingStrategy()

    @Test
    fun `selection starts and ends on the same line before actual changes shouldn't be moved`() {
        val selection = LineRangeMutator(24, 24)
        strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(24, 24), selection)
    }

    @Test
    fun `selection that starts and ends on the same line after actual changes should be moved`() {
        val selection = LineRangeMutator(34, 34)
        strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(29, 29), selection)
    }

    @Test
    fun `selection that start and ends on the same FROM line should be considered as changed`() {
        val selection = LineRangeMutator(29, 29)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(26, 26), selection)
        assertTrue(changed)
    }

    @Test
    fun `selection that starts before actual changed and ends after them should have end moved and considered as changed`() {
        val selection = LineRangeMutator(24, 34)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(24, 29), selection)
        assertTrue(changed)
    }

    @Test
    fun `selection that starts before actual changes and ends after the hunk should have end moved and considered as changed` () {
        val selection = LineRangeMutator(24, 40)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(24, 35), selection)
        assertTrue(changed)

    }

    @Test
    fun `selection that starts after actual changes and ends after the hunk should be moved and NOT considered as changed`() {
        val selection = LineRangeMutator(34, 40)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(29, 35), selection)
        assertFalse(changed)
    }

    @Test
    fun `selection that start on FROM line and ends after actual changes should have end moved and considered as changed` () {
        val selection = LineRangeMutator(26, 34)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(26, 29), selection)
        assertTrue(changed)
    }

    @Test
    fun `selection that start on FROM line and ends after the hunk should have end moved and considered as changed` () {
        val selection = LineRangeMutator(26, 40)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD)
        assertEquals(LineRangeMutator(26, 35), selection)
        assertTrue(changed)
    }

    @Test
    fun `REVERSED selection that starts before actual changed and ends after them should have end moved and considered as changed`() {
        val selection = LineRangeMutator(24, 34)
        val changed = strategy.processHunk(selection, DIFF_6DELETE_1ADD, Direction.REVERSE)
        assertEquals(LineRangeMutator(24, 39), selection)
        assertTrue(changed)
    }


    companion object {
        val DIFF_6DELETE_1ADD = UnifiedDiffParser().parse("""
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

        """.toByteArray())[0].latestHunk
    }
}