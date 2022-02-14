package dev.ithurts.domain.debtevent

import org.springframework.data.repository.CrudRepository

interface DebtEventRepository: CrudRepository<DebtEvent, String> {
    fun findByDebtIdOrderByCreatedAtDesc(debtId: String): List<DebtEvent>
    fun countByDebtId(debtId: String): Int
}