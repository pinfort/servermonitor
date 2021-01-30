package me.pinfort.servermonitor.entity

data class IncidentApi(
    val name: String,
    val status: String,
    val impactOverride: String,
    val body: String,
    val components: Map<String, String>,
    val componentIds: List<String>
)
