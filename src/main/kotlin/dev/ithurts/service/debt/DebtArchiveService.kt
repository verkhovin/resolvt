package dev.ithurts.service.debt

import dev.ithurts.service.debt.model.Debt
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Service

@Service
class DebtArchiveService(private val mongoOperations: MongoOperations) {
    fun archiveDebt(debt: Debt) {
        mongoOperations.save(debt, "debt-arch")
    }
}