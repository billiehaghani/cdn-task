package com.bitmovin.platform.challenge.scheduling.concrete

import com.bitmovin.platform.challenge.client.aws.AwsDataUsageService
import com.bitmovin.platform.challenge.client.dto.DataUsageDto
import com.bitmovin.platform.challenge.persistence.CdnSetup
import com.bitmovin.platform.challenge.persistence.CdnSetupRepository
import com.bitmovin.platform.challenge.persistence.DataUsage
import com.bitmovin.platform.challenge.persistence.DataUsageRepository
import com.bitmovin.platform.challenge.scheduling.DataUsageSynchronizer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Component
class DataUsageSynchronizerImpl(
    private val awsDataUsageService: AwsDataUsageService,
    private val cdnSetupRepository: CdnSetupRepository,
    private val dataUsageRepository: DataUsageRepository,
    private val rabbitTemplate: RabbitTemplate,
    @Value("\${cdn-task.rabbitmq.cap.exchange-name}")
    private val capExchangeName: String,
    @Value("\${cdn-task.rabbitmq.cap.routing-key}")
    private val capRoutingKey: String,
) : DataUsageSynchronizer {
    @Scheduled(
        initialDelay = 0,
        fixedRateString = "\${cdn-task.scheduler.data-usage.rate}",
        zone = "\${cdn-task.scheduler.data-usage.time-zone}",
    )
    override fun fetchDataUsage() {
        val now = Instant.now()
        withLoggingContext("start-time" to now.toString()) {
            logger.info { "Fetching usage data..." }
        }
        val dataUsageInLast15Minutes = awsDataUsageService.getDataUsage(now.minus(15, ChronoUnit.MINUTES), now)
        if (dataUsageInLast15Minutes.isNotEmpty()) {
            val totalUsageAmountInLast15Minutes = calculateTotalUsageAmount(dataUsageInLast15Minutes)
            if (totalUsageAmountInLast15Minutes > 100) {
                disableCdnAndSendMessage(dataUsageInLast15Minutes.first().distributionId)
            }
            persistDataUsages(dataUsageInLast15Minutes)
        }

        val dataUsageInLast3Hours = awsDataUsageService.getDataUsage(now.minus(3, ChronoUnit.HOURS), now)
        if (dataUsageInLast3Hours.isNotEmpty()) {
            val totalUsageAmountInLast3Hours = calculateTotalUsageAmount(dataUsageInLast3Hours)
            if (totalUsageAmountInLast3Hours > 500) {
                disableCdnAndSendMessage(dataUsageInLast3Hours.first().distributionId)
            }
        }
    }

    private fun persistDataUsages(dataUsages: List<DataUsageDto>) {
        val distributionId = dataUsages.first().distributionId
        dataUsages.map { it.values }.flatten().forEach {
            DataUsage().apply {
                this.distributionId = distributionId
                this.time = LocalDate.ofInstant(it.first, ZoneId.systemDefault())
                this.value = it.second
            }.also {
                dataUsageRepository.saveAndFlush(it)
            }
        }
    }

    private fun disableCdnAndSendMessage(distributionId: String) {
        cdnSetupRepository.findByDistributionId(distributionId).apply {
            this!!.status = CdnSetup.Status.CAP_REACHED
        }.also {
            cdnSetupRepository.saveAndFlush(it!!)
            val message =
                MessageBuilder
                    .withBody(
                        """
                        {
                            "type":"EVENT_CDN_CAP_REACHED",
                            "customerId":"${it.customerId}"
                        }
                        """.trimIndent().toByteArray(),
                    )
                    .build()
            rabbitTemplate.send(capExchangeName, capRoutingKey, message)
        }
    }

    private fun calculateTotalUsageAmount(dataUsage: List<DataUsageDto>): Double {
        return dataUsage.map { it.values }.flatten().sumOf { it.second }
    }
}
