package dev.ithurts.service

import dev.ithurts.model.debt.Debt
import dev.ithurts.repository.DebtRepository
import dev.ithurts.security.IntegrationAuthenticationFacade
import org.springframework.stereotype.Service

@Service
class OrganisationAwareDebtService(
    private val debtRepository: DebtRepository,
    private val integrationAuthenticationFacade: IntegrationAuthenticationFacade
) {
    fun getDebtsForFiles(paths: List<String>): List<Debt> {
        val organisation = integrationAuthenticationFacade.organisation
        return debtRepository.findByOrganisationIdAndFilePathIn(organisation.id!!, paths)
    }

    fun saveDebt(debt: Debt) {
        debtRepository.save(debt)
    }
}