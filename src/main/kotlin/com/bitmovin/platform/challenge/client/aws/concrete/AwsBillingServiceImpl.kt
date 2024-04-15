package com.bitmovin.platform.challenge.client.aws.concrete

import com.bitmovin.platform.challenge.client.aws.AwsBillingService
import com.bitmovin.platform.challenge.client.dto.BillingDto
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageRequest
import software.amazon.awssdk.services.costexplorer.model.Group
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Component
class AwsBillingServiceImpl(
    private val costExplorerClient: CostExplorerClient,
) : AwsBillingService {
    override fun getBillingData(
        from: Instant,
        to: Instant,
    ): List<BillingDto> {
        withLoggingContext("from" to from.toString(), "to" to to.toString()) {
            logger.info { "Retrieving billing data..." }
        }
        val getCostAndUsageRequest = GetCostAndUsageRequest.builder().build()
        val result =
            costExplorerClient.getCostAndUsage(getCostAndUsageRequest).resultsByTime().map {
                BillingDto(
                    distributionId = it.groups().first().keys().first(),
                    from = Instant.parse(it.timePeriod().start()),
                    to = Instant.parse(it.timePeriod().end()),
                    values = constructValues(it.groups()),
                )
            }
        return result
    }

    private fun constructValues(groups: List<Group>): List<Pair<String, Double>> {
        return groups.map {
            it.metrics().map { metric ->
                metric.key to metric.value.amount().toDouble()
            }.toList()
        }.flatten()
    }
}
