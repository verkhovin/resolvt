package dev.ithurts.domain.debt

import io.reflectoring.diffparser.api.model.Diff

interface DiffApplier {
    fun applyDiffs(binding: Binding, diffs: List<Diff>): List<BindingChange>
}
