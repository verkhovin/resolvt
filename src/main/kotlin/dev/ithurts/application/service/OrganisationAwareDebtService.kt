package dev.ithurts.application.service

import dev.ithurts.domain.debt.Debt
import dev.ithurts.domain.debt.DebtRepository
import dev.ithurts.application.security.IntegrationAuthenticationFacade
import org.springframework.stereotype.Service

@Service
class OrganisationAwareDebtService(
    private val debtRepository: DebtRepository,
    private val integrationAuthenticationFacade: IntegrationAuthenticationFacade
) {
    fun getDebtsForFiles(paths: List<String>): List<Debt> {
        val organisation = integrationAuthenticationFacade.workspace
        return debtRepository.findByOrganisationIdAndFilePathIn(organisation.identity!!, paths)
    }

    fun saveDebt(debt: Debt) {
        debtRepository.save(debt)
    }
}