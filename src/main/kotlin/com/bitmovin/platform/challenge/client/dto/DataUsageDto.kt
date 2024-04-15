package com.bitmovin.platform.challenge.client.dto

import java.time.Instant

data class DataUsageDto(
    val distributionId: String,
    val values: List<Pair<Instant, Double>>,
)
