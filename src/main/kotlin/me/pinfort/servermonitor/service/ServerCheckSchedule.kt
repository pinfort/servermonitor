package me.pinfort.servermonitor.service

import me.pinfort.servermonitor.component.statusCheck.http.Head
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class ServerCheckSchedule(
    private val serverCheckConfigurationProperties: ServerCheckConfigurationProperties,
    private val httpHead: Head,
) {
    fun execute() {
        val results = httpHead.check()
    }
}
