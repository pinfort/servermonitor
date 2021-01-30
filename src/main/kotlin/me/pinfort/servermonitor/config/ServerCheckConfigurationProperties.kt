package me.pinfort.servermonitor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "server-check")
class ServerCheckConfigurationProperties(
    var pageId: String = "",
    var http: Http = Http(),
    var components: List<Components> = listOf(Components())
) {
    class Http(
        var head: List<Target> = listOf(Target())
    )

    class Target(
        var host: String = "",
        var name: String = ""
    )

    class Components(
        var name: String = "",
        var componentId: String = ""
    )
}
