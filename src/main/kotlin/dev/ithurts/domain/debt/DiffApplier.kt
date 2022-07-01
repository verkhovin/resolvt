package dev.ithurts.domain.debt

import io.reflectoring.diffparser.api.model.Diff

interface DiffApplier {
    fun applyDiffs(debt: Debt, binding: Binding, diffs: List<Diff>, commitHash: String): List<BindingChange>
}
