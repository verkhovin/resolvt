package dev.ithurts.domain

import dev.ithurts.domain.debt.Debt

class CostCalculationService {
    fun calculateCost(debt: Debt, debtEventsCount: Int): Int {
        return debt.votes.size * 5 + debtEventsCount * 10
    }
}