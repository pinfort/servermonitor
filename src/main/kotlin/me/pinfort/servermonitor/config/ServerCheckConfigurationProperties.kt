package me.pinfort.servermonitor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "server-check")
class ServerCheckConfigurationProperties(
    var http: Http = Http()
) {
    class Http(
        var head: List<Target> = listOf(Target())
    )

    class Target(
        var host: String = "",
        var name: String = ""
    )
}
