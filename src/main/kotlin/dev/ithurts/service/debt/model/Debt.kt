package dev.ithurts.service.debt.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "debts")
data class Debt(
    val title: String,
    val description: String,
    val status: DebtStatus,
    val creatorAccountId: String,
    val repositoryId: String,
    val workspaceId: String,
    val bindings: List<Binding>,
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
    val votes: List<DebtVote> = emptyList(),
    @Id
    val _id: String? = null,
) {
    val id: String
        get() = _id!!

    fun update(
        title: String,
        description: String,
        status: DebtStatus,
        updatedAt: Instant,
    ): Debt {
        return this.copy(
            title = title,
            description = description,
            status = status,
            updatedAt = updatedAt
        )
    }


    /**
     * Sets the binding list.
     * Existing bindings that are missed from [bindings] are marked as archived
     */
    fun rebind(bindings: List<Binding>): Debt {
        /*
        If binding status is null, that means that plugin sent request is of version that is not aware about binding statuses.
        In this case, we just get current status of that binding if it's already was saved, otherwise set ACTIVE
         */
        val bindingsWithStatuses = bindings.map {
            if (it.status != null) {
                it
            } else {
                val currentStatus = this.bindings.firstOrNull { existingBinding -> existingBinding.id == it.id }?.status
                it.copy(status = currentStatus ?: BindingStatus.ACTIVE)
            }
        }
        val bindingIds = bindingsWithStatuses.map { it.id }
        val missingBindings = this.bindings
            .filter { existingBinding -> existingBinding.id !in bindingIds }
            .map { it.archive() }
        return this.copy(bindings = bindingsWithStatuses + missingBindings)
    }

    fun updateBinding(bindingId: String, path: String, startLine: Int, endLine: Int): Debt {
        val currentBindings = this.bindings
        val binding = currentBindings.find { it.id == bindingId } ?: throw IllegalArgumentException("Binding not found")
        return rebind(binding.update(path, startLine, endLine))
    }

    fun updateAdvancedBinding(
        bindingId: String,
        path: String,
        parent: String?,
        name: String,
        params: List<String>,
    ): Debt {
        val binding = bindings.find { it.id == bindingId } ?: throw IllegalArgumentException("Binding not found")
        if (!binding.isAdvanced()) {
            throw IllegalArgumentException("Binding is not advanced")
        }
        return rebind(binding.update(filePath = path).updateAdvancedBinding(parent, name, params))
    }

    fun vote(accountId: String): Debt {
        val vote = DebtVote(accountId)
        if (!votes.contains(vote)) {
            return this.copy(
                votes = this.votes + vote
            )
        }
        return this
    }

    fun downVote(accountId: String): Debt {
        val vote = DebtVote(accountId)
        return this.copy(votes = this.votes - vote)
    }

    fun isVotedBy(accountId: String) = this.votes.contains(DebtVote(accountId))

    private fun rebind(binding: Binding): Debt {
        val currentBindings = this.bindings
        currentBindings.firstOrNull { it.id == binding.id }
            ?: throw IllegalArgumentException("Binding should exist: $binding")
        return rebind(
            bindings.filter { it.id != binding.id } + binding
        )
    }
}

enum class DebtStatus {
    OPEN,
    RESOLVED
}