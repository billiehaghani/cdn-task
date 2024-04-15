package com.bitmovin.platform.challenge.client.dto

import java.time.Instant

data class BillingDto(
    val distributionId: String,
    val from: Instant,
    val to: Instant,
    val values: List<Pair<String, Double>>,
)
