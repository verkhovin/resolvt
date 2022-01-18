package dev.ithurts.controller.web

import dev.ithurts.application.service.DebtApplicationService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/debt")
class DebtController(private val debtApplicationService: DebtApplicationService) {
    @PostMapping("/{debtId}/vote")
    fun vote(@PathVariable debtId: Long): ResponseEntity<Any> {
        debtApplicationService.vote(debtId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{debtId}/downVote")
    fun downVote(@PathVariable debtId: Long): ResponseEntity<Any> {
        debtApplicationService.vote(debtId)
        return ResponseEntity.ok().build()
    }
}