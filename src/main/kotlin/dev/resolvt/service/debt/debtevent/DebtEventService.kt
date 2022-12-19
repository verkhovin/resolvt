package dev.resolvt.service.debt.debtevent

import org.springframework.stereotype.Service
import java.time.Clock

@Service
class DebtEventService(
    private val debtEventRepository: DebtEventRepository,
    private val clock: Clock
) {
    fun saveEvent(event: DebtEvent) {
        if (event.changes.isEmpty()) {
            return
        }
        debtEventRepository.save(event)
    }
}