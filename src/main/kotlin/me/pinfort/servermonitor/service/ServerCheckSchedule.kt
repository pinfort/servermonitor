package me.pinfort.servermonitor.service

import com.fasterxml.jackson.databind.ObjectMapper
import me.pinfort.servermonitor.component.statusCheck.http.Head
import me.pinfort.servermonitor.component.statusReport.StatusPage
import org.slf4j.Logger
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class ServerCheckSchedule(
    private val httpHead: Head,
    private val statusPage: StatusPage,
    private val logger: Logger
) {
    @Scheduled(cron = "12 13 * * * *")
    fun execute() {
        logger.info("Scheduled server status check started.")
        val results = httpHead.check()
        val statuses = results.map { it.name to it.status }
        logger.debug("server check status corrected. statuses=${ObjectMapper().writeValueAsString(statuses)}")
        statusPage.report(statuses)
        logger.info("Scheduled server status check finished.")
    }
}
