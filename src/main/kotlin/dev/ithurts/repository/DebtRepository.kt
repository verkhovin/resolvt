package dev.ithurts.repository

import dev.ithurts.model.debt.Debt
import org.springframework.data.repository.CrudRepository

interface DebtRepository: CrudRepository<Debt, Long> {
}