package dev.ithurts.domain.bindingevent

import org.springframework.data.repository.CrudRepository

interface BindingEventRepository: CrudRepository<BindingEvent, String> {
    fun findByDebtId(debtId: String): List<BindingEvent>
}