package org.example.hamsteractivitytracker.service

import org.springframework.stereotype.Service

@Service
class SimpleAlertService : AlertService {
    override suspend fun sendAlert(message: String) {
        println("ALERT: $message")
    }
}