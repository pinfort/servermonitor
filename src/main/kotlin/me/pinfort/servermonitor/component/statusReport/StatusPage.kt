package me.pinfort.servermonitor.component.statusReport

import kotlinx.coroutines.runBlocking
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import me.pinfort.servermonitor.entity.IncidentApi
import me.pinfort.servermonitor.enum.ServerStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

@Component
class StatusPage(
    private val serverCheckConfigurationProperties: ServerCheckConfigurationProperties,
    private val webClient: WebClient
) {
    fun report(targets: List<Pair<String, ServerStatus>>) {
        val statuses: MutableList<Pair<String, ServerStatus>> = mutableListOf()
        targets.filter { it.second != ServerStatus.LIVE }.forEach {
            val component = serverCheckConfigurationProperties.components.find { component -> it.first == component.name }
                ?: return@forEach
            statuses.add(component.componentId to it.second)
        }
        if (statuses.isEmpty()) {
            return
        }
        val incidentName: String = statuses.joinToString(", ") {
            serverCheckConfigurationProperties.components.find { component -> it.first == component.componentId }?.name
                ?: ""
        } + " outage"
        runBlocking {
            val job = kickApi(statuses.map { it.first }, incidentName)
            val responseEntity = job.block()
            if (responseEntity?.statusCode?.is2xxSuccessful != true) {
                // error
            }
        }
    }

    suspend fun kickApi(componentIds: List<String>, incidentName: String): Mono<ResponseEntity<Void>> {
        val components: MutableMap<String, String> = mutableMapOf()
        componentIds.forEach { components.put(it, "major_outage") }
        val incidentObject = IncidentApi(
            name = incidentName,
            status = "investigating",
            impactOverride = "critical",
            body = "This is incident created by monitoring system. Admin will check and update this.",
            components = components.toMap(),
            componentIds = componentIds
        )
        val pageId = serverCheckConfigurationProperties.pageId
        return webClient.post().uri(URI("https://api.statuspage.io/v1/pages/${pageId}/incidents"))
            .body(BodyInserters.fromValue(incidentObject))
            .retrieve().toBodilessEntity()
    }
}
