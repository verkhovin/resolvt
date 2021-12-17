package dev.ithurts.service

import dev.ithurts.model.debt.DebtStatus
import io.reflectoring.diffparser.api.UnifiedDiffParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import dev.ithurts.debtMock

class HunkResolvingStrategyTest {

    private val strategy = HunkResolvingStrategy()
    private val diffParser = UnifiedDiffParser()

    @Test
    fun `debt starts and ends on the same line before actual changes shouldn't be moved`() {
        val debt = debtMock(24, 24)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(24, debt.startLine)
        assertEquals(24, debt.endLine)
    }

    @Test
    fun `debt that starts and ends on the same line after actual changes should be moved`() {
        val debt = debtMock(34, 34)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(29, debt.startLine)
        assertEquals(29, debt.endLine)
    }

    @Test
    fun `debt that start and ends on the same FROM line should be marked as probably resolved`() {
        val debt = debtMock(29, 29)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(26, debt.startLine)
        assertEquals(26, debt.endLine)
        assertEquals(debt.status, DebtStatus.PROBABLY_RESOLVED_CODE_DELETED)
    }

    @Test
    fun `debt that starts before actual changed and ends after them should have end moved and marked as probably resolved`() {
        val debt = debtMock(24, 34)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(24, debt.startLine)
        assertEquals(29, debt.endLine)
        assertEquals(debt.status, DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED)
    }

    @Test
    fun `debt that starts before actual changes and ends after the hunk should have end moved and marked as probably resolved` () {
        val debt = debtMock(24, 40)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(24, debt.startLine)
        assertEquals(35, debt.endLine)
        assertEquals(debt.status, DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED)
    }

    @Test
    fun `debt that starts after actual changes and ends after the hunk should be moved and status shouldn't be changed`() {
        val debt = debtMock(34, 40)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(29, debt.startLine)
        assertEquals(35, debt.endLine)
        assertEquals(debt.status, DebtStatus.OPEN)
    }

    @Test
    fun `debt that start on FROM line and ends after actual changes should have end moved and marked as probably resolved` () {
        val debt = debtMock(26, 34)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(26, debt.startLine)
        assertEquals(29, debt.endLine)
        assertEquals(debt.status, DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED)
    }

    @Test
    fun `debt that start on FROM line and ends after the hunk should have end moved and marked as probably resolved` () {
        val debt = debtMock(26, 40)
        strategy.processHunk(debt, hunk(DIFF_6DELETE_1ADD))
        assertEquals(26, debt.startLine)
        assertEquals(35, debt.endLine)
        assertEquals(debt.status, DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED)
    }

    private fun hunk(diff: String) = diffParser.parse(diff.toByteArray())[0].hunks[0]

    companion object {
        const val DIFF_6DELETE_1ADD = """
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

        """
    }
}