package me.pinfort.servermonitor.component.statusCheck.http

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import me.pinfort.servermonitor.entity.StatusCheckResult
import me.pinfort.servermonitor.enum.ServerStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


class HeadTest {
    @MockK
    private lateinit var webClient: WebClient

    @MockK
    private lateinit var serverCheckConfigurationProperties: ServerCheckConfigurationProperties

    @MockK
    private lateinit var mono: Mono<ResponseEntity<Void>>

    @RelaxedMockK
    private lateinit var logger: Logger

    @InjectMockKs
    private lateinit var component: Head

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { serverCheckConfigurationProperties.http.head } returns listOf(ServerCheckConfigurationProperties.Target("example.com", "example"))
        every { webClient.head().uri(ofType(String::class)).retrieve().toBodilessEntity() } returns mono
    }

    @Test
    fun check_success() {
        every { mono.block() } returns ResponseEntity.ok().build()

        val actual = component.check()
        val expected = listOf(StatusCheckResult(
            name = "example",
            status = ServerStatus.LIVE
        ))
        Assertions.assertIterableEquals(expected, actual)
    }

    @Test
    fun check_network_error() {
        every { mono.block() } throws RuntimeException()

        val actual = component.check()
        val expected = listOf(StatusCheckResult(
            name = "example",
            status = ServerStatus.DEAD
        ))
        Assertions.assertIterableEquals(expected, actual)
    }

    @Test
    fun check_5xx() {
        every { mono.block() } returns ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

        val actual = component.check()
        val expected = listOf(StatusCheckResult(
            name = "example",
            status = ServerStatus.DEAD
        ))
        Assertions.assertIterableEquals(expected, actual)
    }
}
