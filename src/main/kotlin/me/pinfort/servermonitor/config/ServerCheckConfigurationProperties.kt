package me.pinfort.servermonitor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "server-check")
class ServerCheckConfigurationProperties(
    val http: Http
) {
    inner class Http(
        val head: List<Target>
    )

    inner class Target(
        val host: String,
        val name: String
    )
}
