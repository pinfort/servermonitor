package me.pinfort.servermonitor.component.statusReport

import kotlinx.coroutines.runBlocking
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import me.pinfort.servermonitor.entity.IncidentApi
import me.pinfort.servermonitor.enum.ServerStatus
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

@Component
class StatusPage(
    private val serverCheckConfigurationProperties: ServerCheckConfigurationProperties,
    private val webClient: WebClient,
    private val logger: Logger
) {
    fun report(targets: List<Pair<String, ServerStatus>>): Boolean {
        val statuses: MutableList<Pair<String, ServerStatus>> = mutableListOf()
        targets.filter { it.second != ServerStatus.LIVE }.forEach {
            val component = serverCheckConfigurationProperties.components.find { component -> it.first == component.name }
                ?: return@forEach
            statuses.add(component.componentId to it.second)
        }
        if (statuses.isEmpty()) {
            logger.info("No incident found. This will not be reported to StatusPage.")
            return true
        }
        val incidentName: String = statuses.joinToString(", ") {
            serverCheckConfigurationProperties.components.find { component -> it.first == component.componentId }?.name
                ?: ""
        } + " outage"
        return runBlocking {
            try {
                val job = kickApi(statuses.map { it.first }, incidentName)
                val responseEntity = job.block()
                if (responseEntity?.statusCode?.is2xxSuccessful != true) {
                    logger.error("StatusPage API returned error response code. responseCode=${responseEntity?.statusCode.toString()}")
                    return@runBlocking false
                }
            } catch (e: RuntimeException) {
                logger.error("kick StatusPage API failed.")
                return@runBlocking false
            }
            return@runBlocking true
        }
    }

    suspend fun kickApi(componentIds: List<String>, incidentName: String): Mono<ResponseEntity<Void>> {
        val components: MutableMap<String, String> = mutableMapOf()
        componentIds.forEach { components[it] = "major_outage" }
        val incidentObject = IncidentApi(
            name = incidentName,
            status = "investigating",
            impactOverride = "critical",
            body = "This is incident created by monitoring system. Admin will check and update this.",
            components = components.toMap(),
            componentIds = componentIds
        )
        val pageId = serverCheckConfigurationProperties.pageId
        return webClient.post().uri(URI("https://api.statuspage.io/v1/pages/$pageId/incidents"))
            .body(BodyInserters.fromValue(incidentObject))
            .retrieve().toBodilessEntity()
    }
}
