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
import software.amazon.awssdk.services.costexplorer.CostExplorerClient
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageRequest
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageResponse
import java.time.Instant

@ExtendWith(value = [MockKExtension::class, OutputCaptureExtension::class])
internal class AwsBillingServiceImplTest {
    @MockK
    private lateinit var costExplorerClient: CostExplorerClient

    @InjectMockKs
    private lateinit var awsBillingServiceImpl: AwsBillingServiceImpl

    @Test
    fun `when invocation of getBillingData is successful, verification should succeed`(output: CapturedOutput) {
        every {
            costExplorerClient.getCostAndUsage(any<GetCostAndUsageRequest>())
        } returns GetCostAndUsageResponse.builder().build()

        val now = Instant.now()
        awsBillingServiceImpl.getBillingData(now, now)

        verify(exactly = 1) {
            costExplorerClient.getCostAndUsage(any<GetCostAndUsageRequest>())
        }
        confirmVerified(costExplorerClient)
        assertThat(output).contains("Retrieving billing data")
    }
}
