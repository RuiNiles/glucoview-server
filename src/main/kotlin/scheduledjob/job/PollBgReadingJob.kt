package com.ruiniles.scheduledjob.job

import com.ruiniles.LibreClient
import com.ruiniles.firebase.client.FirebaseClient
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.slf4j.Logger
import java.time.LocalDateTime.now

class PollBgReadingJob(
    val libreClient: LibreClient,
    val firebaseClient: FirebaseClient,
    val logger: Logger
) : Job {
    var previousTimestamp: LocalDateTime? = null

    override fun run() {
        logger.info("Starting Job at: ${now()}")

        runCatching {
            val newValue = runBlocking { libreClient.fetchGraph() }

            logger.info("Retrieved value: ${newValue.value}")

            if (newValue.timestamp != previousTimestamp) {
                logger.info("Sending FCM")
                firebaseClient.sendBgReadingToDevice(newValue)
                previousTimestamp = newValue.timestamp
            }
        }.onFailure {
            logger.info("Job Failed: ${it.cause} ${it.message}")
        }

        logger.info("Finishing Job at: ${now()}")
    }
}
