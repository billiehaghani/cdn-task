package com.bitmovin.platform.challenge.scheduling.concrete

import com.bitmovin.platform.challenge.client.aws.AwsDataUsageService
import com.bitmovin.platform.challenge.persistence.CdnSetupRepository
import com.bitmovin.platform.challenge.persistence.DataUsageRepository
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.confirmVerified
import io.mockk.verify
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@SpringBootTest(properties = ["cdn-task.scheduler.data-usage.rate=PT2S"])
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class DataUsageSynchronizerImplTest {
    @SpykBean
    private lateinit var dataUsageSynchronizerImpl: DataUsageSynchronizerImpl

    @MockkBean(relaxed = true)
    private lateinit var awsDataUsageService: AwsDataUsageService

    @MockkBean(relaxed = true)
    private lateinit var cdnSetupRepository: CdnSetupRepository

    @MockkBean(relaxed = true)
    private lateinit var dataUsageRepository: DataUsageRepository

    companion object {
        @Container
        @ServiceConnection
        val mySQLContainer: MySQLContainer<*> = MySQLContainer("mysql:8.0")
    }

    @Test
    fun `when 5 seconds passed, scheduled task should be executed twice`() {
        await atMost Duration.ofSeconds(5) untilAsserted {
            verify(exactly = 2) {
                dataUsageSynchronizerImpl.fetchDataUsage()
            }
        }
        confirmVerified(dataUsageSynchronizerImpl)
    }
}
