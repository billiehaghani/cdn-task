package com.bitmovin.platform.challenge.client.aws.concrete

import com.bitmovin.platform.challenge.client.aws.AwsDataUsageService
import com.bitmovin.platform.challenge.client.dto.DataUsageDto
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest
import software.amazon.awssdk.services.cloudwatch.model.Metric
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery
import software.amazon.awssdk.services.cloudwatch.model.MetricStat
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Component
class AwsDataUsageServiceImpl(
    private val cloudWatchClient: CloudWatchClient,
) : AwsDataUsageService {
    override fun getDataUsage(
        from: Instant,
        to: Instant,
    ): List<DataUsageDto> {
        withLoggingContext("from" to from.toString(), "to" to to.toString()) {
            logger.info { "Retrieving usage data..." }
        }
        val getMetricDataRequest = constructGetMetricDataRequest(from, to)
        val result =
            cloudWatchClient.getMetricData(getMetricDataRequest).metricDataResults().map {
                DataUsageDto(
                    distributionId = it.label(),
                    values = it.timestamps().zip(it.values()),
                )
            }
        return result
    }

    private fun constructGetMetricDataRequest(
        from: Instant,
        to: Instant,
    ): GetMetricDataRequest =
        GetMetricDataRequest.builder()
            .metricDataQueries(
                MetricDataQuery.builder()
                    .metricStat(
                        MetricStat.builder()
                            .metric(
                                Metric.builder()
                                    .metricName("Usage")
                                    .namespace("AWS/S3")
                                    .build(),
                            )
                            .period(60)
                            .stat("AMT")
                            .build(),
                    )
                    .id("Usage")
                    .expression("*")
                    .build(),
            )
            .startTime(from)
            .endTime(to)
            .build()
}
