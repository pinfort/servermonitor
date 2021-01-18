package me.pinfort.servermonitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ServermonitorApplication

fun main(args: Array<String>) {
	runApplication<ServermonitorApplication>(*args)
}
