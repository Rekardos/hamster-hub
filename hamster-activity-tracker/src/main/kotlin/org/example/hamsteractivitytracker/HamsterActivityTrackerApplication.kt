package org.example.hamsteractivitytracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HamsterActivityTrackerApplication

fun main(args: Array<String>) {
	runApplication<HamsterActivityTrackerApplication>(*args)
}
