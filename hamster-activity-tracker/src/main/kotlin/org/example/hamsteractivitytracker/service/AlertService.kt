package org.example.hamsteractivitytracker.service

interface AlertService {
    suspend fun sendAlert(message: String)
}