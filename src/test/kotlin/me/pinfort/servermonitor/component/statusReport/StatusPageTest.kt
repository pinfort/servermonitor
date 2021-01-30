package me.pinfort.servermonitor.component.statusReport

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import me.pinfort.servermonitor.enum.ServerStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class StatusPageTest {
    @MockK
    private lateinit var serverCheckConfigurationProperties: ServerCheckConfigurationProperties

    @MockK
    private lateinit var webClient: WebClient

    @MockK
    private lateinit var mono: Mono<ResponseEntity<Void>>

    @RelaxedMockK
    private lateinit var logger: Logger

    @InjectMockKs
    private lateinit var component: StatusPage

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { serverCheckConfigurationProperties.components } returns listOf(ServerCheckConfigurationProperties.Components(name = "example", componentId = "dummy"))
        every { serverCheckConfigurationProperties.pageId } returns "dummy"
    }

    @Test
    fun report_success() {
        every { webClient.post().uri(ofType(String::class)).body(any()).retrieve().toBodilessEntity() } returns mono
        every { mono.block() } returns ResponseEntity.ok().build()

        val statuses = listOf(
            "example" to ServerStatus.DEAD
        )

        Assertions.assertTrue(component.report(statuses))

        verify(exactly = 1) {
            mono.block()
        }
    }

    @Test
    fun report_no_incident() {
        val statuses = listOf(
            "example" to ServerStatus.LIVE
        )

        Assertions.assertTrue(component.report(statuses))

        verify(exactly = 0) {
            mono.block()
        }
    }

    @Test
    fun report_network_error() {
        every { webClient.post().uri(ofType(String::class)).body(any()).retrieve().toBodilessEntity() } returns mono
        every { mono.block() } throws RuntimeException()
        val statuses = listOf(
            "example" to ServerStatus.DEAD
        )
        Assertions.assertFalse(component.report(statuses))

        verify(exactly = 1) {
            mono.block()
        }
    }

    @Test
    fun report_5xx() {
        every { webClient.post().uri(ofType(String::class)).body(any()).retrieve().toBodilessEntity() } returns mono
        every { mono.block() } returns ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        val statuses = listOf(
            "example" to ServerStatus.DEAD
        )
        Assertions.assertFalse(component.report(statuses))

        verify(exactly = 1) {
            mono.block()
        }
    }
}
