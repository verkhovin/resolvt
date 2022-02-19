package dev.ithurts.application.internal

import dev.ithurts.domain.debt.Debt
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Service

@Service
class ArchiveService(private val mongoOperations: MongoOperations) {
    fun archiveDebt(debt: Debt) {
        mongoOperations.save(debt, "debt-arch")
    }
}