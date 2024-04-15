package com.bitmovin.platform.challenge.persistence

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class DataUsageRepositoryTest {
    @Autowired
    private lateinit var dataUsageRepository: DataUsageRepository

    companion object {
        @Container
        @ServiceConnection
        val mySQLContainer: MySQLContainer<*> = MySQLContainer("mysql:8.0")
    }

    @Test
    fun `when a DataUsage with the given distributionId exists, returns the DataUsage`() {
        val distributionId = UUID.randomUUID().toString()
        val now = LocalDate.now()
        dataUsageRepository.save(
            DataUsage().apply {
                this.distributionId = distributionId
                this.value = Random.nextDouble()
                this.time = now.minusDays(1)
            },
        )
        dataUsageRepository.save(
            DataUsage().apply {
                this.distributionId = distributionId
                this.value = Random.nextDouble()
                this.time = now.minusDays(3)
            },
        )

        val dataUsages = dataUsageRepository.findByDistributionIdAndTimeBetween(distributionId, now.minusDays(2), now)

        assertThat(dataUsages).hasSize(1)
        assertThat(dataUsages.first().distributionId).isEqualTo(distributionId)
        assertThat(dataUsages.first().time).isEqualTo(now.minusDays(1))
    }
}
