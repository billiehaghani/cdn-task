package com.bitmovin.platform.challenge.persistence

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class CdnSetupRepositoryTest {
    @Autowired
    private lateinit var cdnSetupRepository: CdnSetupRepository

    companion object {
        @Container
        @ServiceConnection
        val mySQLContainer: MySQLContainer<*> = MySQLContainer("mysql:8.0")
    }

    @Test
    fun `when a CdnSetup with the given customerId exists, returns the CdnSetup`() {
        val customerId = UUID.randomUUID().toString()
        cdnSetupRepository.save(
            CdnSetup().apply {
                this.customerId = customerId
            },
        )

        val cdnSetup = cdnSetupRepository.findByCustomerId(customerId)

        assertThat(cdnSetup).isNotNull()
        assertThat(cdnSetup!!.id).isNotNull()
        assertThat(cdnSetup!!.customerId).isEqualTo(customerId)
    }
}
