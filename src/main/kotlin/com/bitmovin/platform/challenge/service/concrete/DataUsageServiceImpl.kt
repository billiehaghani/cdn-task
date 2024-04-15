package com.bitmovin.platform.challenge.service.concrete

import com.bitmovin.platform.challenge.persistence.CdnSetupRepository
import com.bitmovin.platform.challenge.persistence.DataUsageRepository
import com.bitmovin.platform.challenge.service.DataUsageService
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DataUsageServiceImpl(
    private val dataUsageRepository: DataUsageRepository,
    private val cdnSetupRepository: CdnSetupRepository,
) : DataUsageService {
    override fun getUsageData(
        customerId: String,
        from: LocalDate,
        to: LocalDate,
    ): Map<LocalDate, Double> {
        val cdnSetup =
            cdnSetupRepository.findByCustomerId(
                customerId,
            ) ?: throw EntityNotFoundException("CdnSetup for customerId $customerId not found")
        val distributionId = cdnSetup.distributionId
        val dataUsages = dataUsageRepository.findByDistributionIdAndTimeBetween(distributionId, from, to)
        return dataUsages.groupBy {
            it.time
        }.mapValues { it.value.sumOf { it.value } }.toSortedMap()
    }
}
