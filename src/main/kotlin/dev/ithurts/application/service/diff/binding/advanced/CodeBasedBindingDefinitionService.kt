package dev.ithurts.application.service.diff.binding.advanced

import dev.ithurts.application.service.diff.BindingSpec
import dev.ithurts.domain.debt.AdvancedBinding

// TODO норм название. Возможно другие пакеты, которые относятся к биндингам, можно переименовать в соответствии с этой идеей
interface CodeBasedBindingDefinitionService {
    fun getBinding(bindingSpec: BindingSpec, advancedBinding: AdvancedBinding, code: String): BindingSpec
}