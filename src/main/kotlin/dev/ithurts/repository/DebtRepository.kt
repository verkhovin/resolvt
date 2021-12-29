package dev.ithurts.repository

import dev.ithurts.model.debt.Debt
import dev.ithurts.model.debt.DebtStatus
import dev.ithurts.model.debt.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface DebtRepository: CrudRepository<Debt, Long> {
    fun findByRepositoryIdAndStatusNot(repositoryId: Long, excludeWithStatus: DebtStatus): List<Debt>
    @Query("SELECT d FROM Debt d JOIN d.repository r WHERE r.organisation.id = :organisationId AND d.status <> :excludeWithStatus")
    fun findByOrganisationIdAndStatusNot(organisationId: Long, excludeWithStatus: DebtStatus): List<Debt>
    @Query("SELECT d FROM Debt d JOIN d.repository r WHERE r.organisation.id = :organisationId AND d.filePath in (:paths)")
    fun findByOrganisationIdAndFilePathIn(organisationId: Long, paths: List<String>): List<Debt>
}