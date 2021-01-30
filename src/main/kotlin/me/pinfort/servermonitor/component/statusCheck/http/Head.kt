package me.pinfort.servermonitor.component.statusCheck.http

import kotlinx.coroutines.runBlocking
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import me.pinfort.servermonitor.entity.StatusCheckResult
import me.pinfort.servermonitor.enum.ServerStatus
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

@Component
class Head(
    private val webClient: WebClient,
    private val serverCheckConfigurationProperties: ServerCheckConfigurationProperties,
    private val logger: Logger
) {
    fun check(): List<StatusCheckResult> {
        val results: MutableList<StatusCheckResult> = mutableListOf()
        runBlocking {
            for (result in serverCheckConfigurationProperties.http.head.map { doRequest(it) }) {
                val targetName = result.first
                val job = result.second
                var response: ResponseEntity<Void>? = null
                try {
                    response = job.block()
                } catch (e: RuntimeException) {
                    logger.error("target server cannot be connected. targetName=$targetName, errorMessage=${e.message}")
                }
                val statusCheckResult = StatusCheckResult(
                    name = targetName,
                    status = if (response?.statusCode?.is2xxSuccessful == true) {
                        logger.info("server is LIVE. targetName=$targetName")
                        ServerStatus.LIVE
                    } else {
                        logger.warn("server is DEAD. targetName=$targetName")
                        ServerStatus.DEAD
                    }
                )
                results.add(statusCheckResult)
            }
        }
        return results
    }

    private suspend fun doRequest(target: ServerCheckConfigurationProperties.Target): Pair<String, Mono<ResponseEntity<Void>>> {
        logger.debug("checking server status by requesting with HTTP HEAD. targetName=${target.name}, targetHost=${target.host}")
        return target.name to webClient.head().uri(target.host).retrieve().toBodilessEntity()
    }
}
