package me.pinfort.servermonitor.entity

import me.pinfort.servermonitor.enum.ServerStatus

data class StatusCheckResult(
    val name: String,
    val status: ServerStatus
)
