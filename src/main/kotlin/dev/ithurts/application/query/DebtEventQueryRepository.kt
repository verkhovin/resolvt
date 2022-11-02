package dev.ithurts.application.query

import dev.ithurts.domain.debtevent.DebtEvent
import dev.ithurts.domain.debtevent.DebtEventRepository
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository

@Repository
class DebtEventQueryRepository(
    private val debtEventRepository: DebtEventRepository,
    private val mongoOperations: MongoOperations,
) {
    fun findByDebtId(debtId: String): List<DebtEvent> {
        return debtEventRepository.findByDebtIdOrderByCreatedAtDesc(debtId)
    }

    fun countByDebtId(debtId: String): Int {
        return debtEventRepository.countByDebtId(debtId)
    }

    fun eventCountForEvents(debtIds: List<String>): Map<String, Int> {
        return mongoOperations.aggregate(
            newAggregation(
                match(Criteria.where("debtId").inValues(debtIds).and("changes.visible").isEqualTo(true)),
                group("debtId").count().`as`("count"),
                project("count").and("_id").`as`("debtId")
            ),
            DebtEvent::class.java,
            CountResult::class.java
        ).mappedResults.associate { it.debtId to it.count }
    }
}

data class CountResult(
    val debtId: String,
    val count: Int
)