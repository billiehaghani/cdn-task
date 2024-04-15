package com.bitmovin.platform.challenge.presentation

import com.bitmovin.platform.challenge.service.DataUsageService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate
import java.util.*

@WebMvcTest(controllers = [DataUsageController::class])
class DataUsageControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var dataUsageService: DataUsageService

    @Test
    fun `when requesting a data usage of an unavailable customerId, it should return 404`() {
        val customerId = UUID.randomUUID().toString()
        val message = "CdnSetup for customerId $customerId not found"
        val now = LocalDate.now()
        every {
            dataUsageService.getUsageData(customerId, now, now)
        } throws EntityNotFoundException(message)

        mockMvc.get("/usage-data?from=$now&to=$now") {
            header("X-Customer-Id", customerId)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isNotFound()
            }
            content {
                json("""{"errors":[{"title":"$message","code":"resource_not_found"}]}""".trimIndent())
            }
        }

        verify(exactly = 1) {
            dataUsageService.getUsageData(customerId, now, now)
        }
        confirmVerified(dataUsageService)
    }

    @Test
    fun `when requesting a data usage of an available customerId, it should return 200`() {
        val customerId = UUID.randomUUID().toString()
        val localDate = LocalDate.parse("2024-02-10")
        every {
            dataUsageService.getUsageData(customerId, localDate, localDate)
        } returns mapOf(localDate.minusDays(1) to 135.34, localDate.minusDays(2) to 531.43)

        mockMvc.get("/usage-data?from=$localDate&to=$localDate") {
            header("X-Customer-Id", customerId)
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status {
                isOk()
            }
            content {
                json(
                    """
                    {
                      "2024-02-09": 135.34,
                      "2024-02-08": 531.43
                    }
                    """.trimIndent(),
                )
            }
        }

        verify(exactly = 1) {
            dataUsageService.getUsageData(customerId, localDate, localDate)
        }
        confirmVerified(dataUsageService)
    }
}
