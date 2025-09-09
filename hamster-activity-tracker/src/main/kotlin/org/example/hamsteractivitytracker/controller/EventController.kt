package org.example.hamsteractivitytracker.controller

import org.example.hamsteractivitytracker.model.HamsterEvent
import org.example.hamsteractivitytracker.service.HamsterActivityService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventController(private val activityService: HamsterActivityService) {

    @PostMapping
    suspend fun processEvent(@RequestBody event: HamsterEvent): ResponseEntity<String> {
        return try {
            activityService.processEvent(event)
            ResponseEntity.ok("Event processed")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Error: ${e.message}")
        }
    }
}