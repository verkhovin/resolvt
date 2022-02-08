package dev.ithurts.application.service.events

import dev.ithurts.domain.bindingevent.BindingEvent
import dev.ithurts.domain.bindingevent.BindingEventRepository
import dev.ithurts.domain.bindingevent.ChangeType
import dev.ithurts.domain.bindingevent.Change
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class DebtBindingChangedEventListener(
    private val bindingEventRepository: BindingEventRepository,
    private val clock: Clock
): ApplicationListener<DebtBindingChangedEvent> {
    override fun onApplicationEvent(event: DebtBindingChangedEvent) {
        val bindingEvent = BindingEvent(
            event.debtId,
            event.bindingId,
            event.repositoryId,
            event.commitHash,
            event.changes.map { Change(ChangeType.valueOf(it.type.name), it.from, it.to)},
            clock.instant()
        )
        bindingEventRepository.save(bindingEvent)
    }
}