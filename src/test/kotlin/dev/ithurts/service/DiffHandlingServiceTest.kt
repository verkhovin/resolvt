package dev.ithurts.service

import dev.ithurts.debtMock
import dev.ithurts.model.debt.DebtStatus
import dev.ithurts.service.diff.DiffHandlingService
import dev.ithurts.service.diff.HunkResolvingStrategy
import io.reflectoring.diffparser.api.UnifiedDiffParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.kotlin.anyOrNull


class DiffHandlingServiceTest {
    val debtService: OrganisationAwareDebtService = mock(OrganisationAwareDebtService::class.java)

    val hunkResolvingStrategy: HunkResolvingStrategy = mock(HunkResolvingStrategy::class.java)

    val diffParser = UnifiedDiffParser()

    val diffHandlingService = DiffHandlingService(hunkResolvingStrategy, diffParser, debtService)

    @Test
    fun `when debt is located before the diff it shouldn't be changed` () {
        val debt = debtMock(5, 10)
        given(debtService.getDebtsForFiles(anyList())).willReturn(listOf(debt))
        diffHandlingService.handleDiff(DIFF_6DELETE_1ADD)
        assertEquals(5, debt.startLine)
        assertEquals(10, debt.endLine)
        assertEquals(DebtStatus.OPEN, debt.status)
        then(hunkResolvingStrategy).should(never()).processHunk(anyOrNull(), anyOrNull())
    }

    @Test
    fun `when debt is located after the diff its position should be adjusted`() {
        val debt = debtMock(36, 45)
        given(debtService.getDebtsForFiles(anyList())).willReturn(listOf(debt))
        diffHandlingService.handleDiff(DIFF_6DELETE_1ADD)
        assertEquals(31, debt.startLine)
        assertEquals(40, debt.endLine)
        assertEquals(DebtStatus.OPEN, debt.status)
        then(hunkResolvingStrategy).should(never()).processHunk(anyOrNull(), anyOrNull())
    }

    @Test
    fun `when debt is wider than hunk the end of debt should be adjusted and debt should be marked as probably resolved`() {
        val debt = debtMock(5, 36)
        given(debtService.getDebtsForFiles(anyList())).willReturn(listOf(debt))
        diffHandlingService.handleDiff(DIFF_6DELETE_1ADD)
        assertEquals(5, debt.startLine)
        assertEquals(31, debt.endLine)
        assertEquals(DebtStatus.PROBABLY_RESOLVED_PARTLY_CHANGED, debt.status)
        then(hunkResolvingStrategy).should(never()).processHunk(anyOrNull(), anyOrNull())
    }

    @Test
    fun `when debt start is inside of the hunk processing should be delegated to HunkProcessingStrategy`() {
        val debt = debtMock(35, 40)
        given(debtService.getDebtsForFiles(anyList())).willReturn(listOf(debt))
        diffHandlingService.handleDiff(DIFF_6DELETE_1ADD)
        then(hunkResolvingStrategy).should(times(1)).processHunk(anyOrNull(), anyOrNull())
    }


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