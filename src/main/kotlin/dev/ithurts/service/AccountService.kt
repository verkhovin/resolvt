package dev.ithurts.service

import dev.ithurts.repository.AccountRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class AccountService(private val accountRepository: AccountRepository) {

}
