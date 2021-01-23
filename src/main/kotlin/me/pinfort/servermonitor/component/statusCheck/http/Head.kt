package me.pinfort.servermonitor.component.statusCheck.http

import kotlinx.coroutines.runBlocking
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import me.pinfort.servermonitor.entity.StatusCheckResult
import me.pinfort.servermonitor.enum.ServerStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

@Component
class Head(
    private val webClient: WebClient,
    private val serverCheckConfigurationProperties: ServerCheckConfigurationProperties
) {
    fun check(): List<StatusCheckResult> {
        val results: MutableList<StatusCheckResult> = mutableListOf()
        runBlocking {
            for (result in serverCheckConfigurationProperties.http.head.map { doRequest(it) }) {
                val targetName = result.first
                val job = result.second
                val statusCheckResult = StatusCheckResult(
                    name = targetName,
                    status = if (job.block()?.statusCode?.is2xxSuccessful == true) ServerStatus.LIVE else ServerStatus.DEAD
                )
                results.add(statusCheckResult)
            }
        }
        return results
    }

    suspend fun doRequest(target: ServerCheckConfigurationProperties.Target): Pair<String, Mono<ResponseEntity<Void>>> {
        return target.name to webClient.head().uri(URI(target.host)).retrieve().toBodilessEntity()
    }
}
