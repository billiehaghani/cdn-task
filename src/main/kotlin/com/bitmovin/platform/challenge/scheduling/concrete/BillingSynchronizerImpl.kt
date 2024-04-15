package com.bitmovin.platform.challenge.scheduling.concrete

import com.bitmovin.platform.challenge.client.aws.AwsBillingService
import com.bitmovin.platform.challenge.client.dto.BillingDto
import com.bitmovin.platform.challenge.persistence.CdnSetupRepository
import com.bitmovin.platform.challenge.scheduling.BillingSynchronizer
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class BillingSynchronizerImpl(
    private val awsBillingService: AwsBillingService,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val cdnSetupRepository: CdnSetupRepository,
    @Value("\${cdn-task.rabbitmq.billing.exchange-name}")
    private val billingExchangeName: String,
    @Value("\${cdn-task.rabbitmq.billing.routing-key}")
    private val billingRoutingKey: String,
) : BillingSynchronizer {
    @Scheduled(
        cron = "\${cdn-task.scheduler.billing.cron}",
        zone = "\${cdn-task.scheduler.billing.time-zone}",
    )
    override fun fetchBilling() {
        val now = Instant.now()
        withLoggingContext("start-time" to now.toString()) {
            logger.info { "Fetching billing data..." }
        }
        val billingData = awsBillingService.getBillingData(now.minus(24, ChronoUnit.HOURS), now)
        val message =
            MessageBuilder
                .withBody(constructBody(billingData))
                .build()
        rabbitTemplate.send(billingExchangeName, billingRoutingKey, message)
    }

    private fun constructBody(billingData: List<BillingDto>): ByteArray {
        val billingMessage =
            BillingMessage(
                payload =
                    BillingMessage.Payload(
                        customerId = cdnSetupRepository.findByDistributionId(billingData.first().distributionId)!!.customerId,
                        startPeriod = billingData.first().from,
                        endPeriod = billingData.first().to,
                        trafficUsageGb = computeTrafficUsage(billingData),
                    ),
            )
        return objectMapper.writeValueAsString(billingMessage).toByteArray()
    }

    private fun computeTrafficUsage(billingData: List<BillingDto>): Double {
        return billingData.map { it.values }.flatten().sumOf { it.second }
    }

    data class BillingMessage(
        val type: String = "EVENT_CDN_USAGE",
        val payload: Payload,
    ) {
        data class Payload(
            val id: String = UUID.randomUUID().toString(),
            val customerId: String,
            val startPeriod: Instant,
            val endPeriod: Instant,
            val trafficUsageGb: Double,
        )
    }
}
