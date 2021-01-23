package me.pinfort.servermonitor.service

import me.pinfort.servermonitor.component.statusCheck.http.Head
import me.pinfort.servermonitor.component.statusReport.StatusPage
import me.pinfort.servermonitor.config.ServerCheckConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class ServerCheckSchedule(
    private val httpHead: Head,
    private val statusPage: StatusPage
) {
    @Scheduled(cron = "12 13 * * * *")
    fun execute() {
        val results = httpHead.check()
        val statuses = results.map { it.name to it.status }
        statusPage.report(statuses)
    }
}
