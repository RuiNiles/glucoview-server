package com.ruiniles.scheduledjob.config

import com.ruiniles.LibreClient
import com.ruiniles.firebase.client.FirebaseClient
import com.ruiniles.scheduledjob.job.PollBgReadingJob
import com.ruiniles.scheduledjob.scheduler.JobScheduler
import io.ktor.server.application.*

fun Application.configureScheduledJob(
    libreClient: LibreClient,
    firebaseClient: FirebaseClient,
    jobScheduler: JobScheduler
) {
    jobScheduler.register(PollBgReadingJob(libreClient, firebaseClient, log))
    jobScheduler.start()
}
