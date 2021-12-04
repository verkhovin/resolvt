package dev.ithurts.repository

import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DebtRepository: CrudRepository<Debt, Long> {
    fun findByRepositoryId(repositoryId: Long): List<Debt>
    @Query("SELECT d FROM Debt d JOIN d.repository r WHERE r.organisation.id = :organisationId")
    fun findByOrganisationId(organisationId: Long): List<Debt>
}