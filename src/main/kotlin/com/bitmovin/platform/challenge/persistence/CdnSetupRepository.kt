package com.bitmovin.platform.challenge.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CdnSetupRepository : JpaRepository<CdnSetup, Int> {
    fun findByDistributionId(distributionId: String): CdnSetup?

    fun findByCustomerId(customerId: String): CdnSetup?
}
