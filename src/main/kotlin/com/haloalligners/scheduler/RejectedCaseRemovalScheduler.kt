package com.haloalligners.scheduler

import com.haloalligners.repository.RejectedCaseRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RejectedCaseRemovalScheduler(private val rejectedCaseRepository: RejectedCaseRepository) {
    val tenDaysAgo: LocalDateTime = LocalDateTime.now().minusDays(10)
    @Scheduled(cron = "0 0 22 * * *")
    fun removeLastTenDaysRejectedCases(){
        val rejectedCases = rejectedCaseRepository.findByRejectedAtAfter(tenDaysAgo)
        rejectedCases.forEach { rejectedCase ->
            rejectedCaseRepository.deleteById(rejectedCase.id!!)
        }
    }
}