package com.bitmovin.platform.challenge.service

import java.time.LocalDate

interface DataUsageService {
    fun getUsageData(
        customerId: String,
        from: LocalDate,
        to: LocalDate,
    ): Map<LocalDate, Double>
}
