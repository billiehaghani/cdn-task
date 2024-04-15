package com.bitmovin.platform.challenge.client.aws

import com.bitmovin.platform.challenge.client.dto.BillingDto
import java.time.Instant

interface AwsBillingService {
    fun getBillingData(
        from: Instant,
        to: Instant,
    ): List<BillingDto>
}
