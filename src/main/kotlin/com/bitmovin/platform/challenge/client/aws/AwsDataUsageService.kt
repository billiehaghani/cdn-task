package com.bitmovin.platform.challenge.client.aws

import com.bitmovin.platform.challenge.client.dto.DataUsageDto
import java.time.Instant

interface AwsDataUsageService {
    fun getDataUsage(
        from: Instant,
        to: Instant,
    ): List<DataUsageDto>
}
