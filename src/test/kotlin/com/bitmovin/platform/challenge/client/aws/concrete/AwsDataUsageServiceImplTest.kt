package com.bitmovin.platform.challenge.client.aws.concrete

import assertk.assertThat
import assertk.assertions.contains
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse
import java.time.Instant

@ExtendWith(value = [MockKExtension::class, OutputCaptureExtension::class])
internal class AwsDataUsageServiceImplTest {
    @MockK
    private lateinit var cloudWatchClient: CloudWatchClient

    @InjectMockKs
    private lateinit var awsDataUsageServiceImpl: AwsDataUsageServiceImpl

    @Test
    fun `when invocation of getMetricData is successful, verification should succeed`(output: CapturedOutput) {
        every {
            cloudWatchClient.getMetricData(any<GetMetricDataRequest>())
        } returns GetMetricDataResponse.builder().build()

        val now = Instant.now()
        awsDataUsageServiceImpl.getDataUsage(now, now)

        verify(exactly = 1) {
            cloudWatchClient.getMetricData(any<GetMetricDataRequest>())
        }
        confirmVerified(cloudWatchClient)
        assertThat(output).contains("Retrieving usage data")
    }
}
