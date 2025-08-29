package org.example.hamsteractivitytracker.controller

import org.example.hamsteractivitytracker.model.HamsterEvent
import org.example.hamsteractivitytracker.service.HamsterActivityTrackerService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventController(
    private val trackerService: HamsterActivityTrackerService
) {

    @PostMapping
    suspend fun receiveEvent(@RequestBody event: HamsterEvent): HttpStatus {
        trackerService.processEvent(event)
        return HttpStatus.ACCEPTED
    }
}