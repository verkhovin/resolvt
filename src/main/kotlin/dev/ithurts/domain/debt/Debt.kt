package dev.ithurts.domain.debt

import dev.ithurts.application.events.DebtBindingChangedEvent
import io.reflectoring.diffparser.api.model.Diff
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "debts")
class Debt(
    title: String,
    description: String,
    status: DebtStatus,
    val creatorAccountId: String,
    val repositoryId: String,
    val workspaceId: String,
    bindings: List<Binding>,
    val createdAt: Instant,
    var updatedAt: Instant = createdAt,
    val votes: MutableList<DebtVote> = mutableListOf(),
    @Id
    var _id: String? = null,
) {
    val id: String
        get() = _id!!
    var title: String = title
        private set
    var description: String = description
        private set
    var status: DebtStatus = status
        private set
    var bindings: List<Binding> = bindings
        private set
        get() = field.map { it.copy() }

    fun update(
        title: String,
        description: String,
        status: DebtStatus,
        updatedAt: Instant,
    ) {
        this.title = title
        this.description = description
        this.status = status
        this.updatedAt = updatedAt
    }

    fun applyDiffs(
        diffs: Map<String, List<Diff>>,
        commitHash: String,
        diffApplier: DiffApplier,
    ): DebtBindingChangedEvent {
        val activeBindings = this.bindings.filter { it.active }
        val changes = activeBindings.associate { binding ->
            binding.id to diffApplier.applyDiffs(binding, diffs[binding.filePath] ?: emptyList())
        }
        val updatedBindings = activeBindings.map { binding -> binding.applyChanges(changes[binding.id] ?: emptyList()) }
        rebind(updatedBindings)
        return DebtBindingChangedEvent(
            this, this.id, repositoryId, commitHash, changes.flatMap { it.value }
        )
    }

    /**
     * Sets the binding list.
     * Existing bindings that are missed from [bindings] are marked as inactive
     */
    fun rebind(bindings: List<Binding>) {
        val bindingIds = bindings.map { it.id }
        val missingBindings = this.bindings
            .filter { existingBinding -> existingBinding.id !in bindingIds }
            .map { it.inactivate() }
        this.bindings = bindings + missingBindings
    }

    fun updateBinding(bindingId: String, path: String, startLine: Int, endLine: Int) {
        val currentBindings = this.bindings
        val binding = currentBindings.find { it.id == bindingId } ?: throw IllegalArgumentException("Binding not found")
        rebindSingleBinding(
            binding.applyChanges(
                binding.deriveChanges(path, false, startLine, endLine)
            )
        )
    }

    fun updateAdvancedBindingManually(
        bindingId: String,
        path: String,
        parent: String?,
        name: String,
        params: List<String>,
    ) {
        val binding = bindings.find { it.id == bindingId } ?: throw IllegalArgumentException("Binding not found")
        if (!binding.isAdvanced()) {
            throw IllegalArgumentException("Binding is not advanced")
        }
        rebindSingleBinding(
            binding.applyAdvancedBindingManualChange(path, parent, name, params)
        )
    }

    fun vote(accountId: String) {
        val vote = DebtVote(accountId)
        if (!votes.contains(vote)) {
            votes.add(vote)
        }
    }

    fun downVote(accountId: String) {
        val vote = DebtVote(accountId)
        votes.remove(vote)
    }

    fun isVotedBy(accountId: String) = this.votes.contains(DebtVote(accountId))

    private fun rebindSingleBinding(binding: Binding) {
        val currentBindings = this.bindings
        currentBindings.firstOrNull { it.id == binding.id } ?: throw IllegalArgumentException("Binding should exist: $binding")
        rebind(
            this.bindings.filter { it.id != binding.id } + binding
        )
    }
}

enum class DebtStatus {
    OPEN,
    RESOLVED
}