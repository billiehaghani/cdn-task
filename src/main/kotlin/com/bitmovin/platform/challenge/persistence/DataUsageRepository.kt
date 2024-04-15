package com.bitmovin.platform.challenge.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DataUsageRepository : JpaRepository<DataUsage, Int> {
    fun findByDistributionIdAndTimeBetween(
        distributionId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<DataUsage>
}
