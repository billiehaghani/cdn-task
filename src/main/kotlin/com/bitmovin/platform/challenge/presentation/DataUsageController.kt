package com.bitmovin.platform.challenge.presentation

import com.bitmovin.platform.challenge.service.DataUsageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/usage-data")
class DataUsageController(
    private val dataUsageService: DataUsageService,
) {
    @GetMapping
    fun getForTimeInterval(
        @RequestHeader(value = "X-Customer-Id")
        customerId: String,
        @RequestParam
        from: LocalDate,
        @RequestParam
        to: LocalDate,
    ): Map<LocalDate, Double> {
        require(!to.isBefore(from).or(to.isAfter(LocalDate.now()))) { "Invalid from/to range" }
        return dataUsageService.getUsageData(customerId, from, to)
    }
}
