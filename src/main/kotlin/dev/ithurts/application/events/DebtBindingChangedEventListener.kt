package dev.ithurts.application.events

import dev.ithurts.domain.debt.DebtBindingChangedEvent
import dev.ithurts.domain.debtevent.DebtEvent
import dev.ithurts.domain.debtevent.DebtEventRepository
import dev.ithurts.domain.debtevent.ChangeType
import dev.ithurts.domain.debtevent.BindingChange
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class DebtBindingChangedEventListener(
    private val debtEventRepository: DebtEventRepository,
    private val clock: Clock
): ApplicationListener<DebtBindingChangedEvent> {
    override fun onApplicationEvent(event: DebtBindingChangedEvent) {
        if (event.changes.isEmpty()) {
            return
        }
        val debtEvent = DebtEvent(
            event.debtId,
            event.repositoryId,
            event.commitHash,
            event.changes.map { BindingChange(it.bindingId, ChangeType.valueOf(it.type.name),it.from, it.to)},
            clock.instant()
        )
        debtEventRepository.save(debtEvent)
    }
}