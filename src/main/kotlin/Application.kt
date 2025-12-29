package com.ruiniles

import com.ruiniles.firebase.client.FirebaseClient
import com.ruiniles.firebase.config.configureFirebase
import com.ruiniles.scheduledjob.config.configureScheduledJob
import com.ruiniles.scheduledjob.scheduler.JobScheduler
import com.ruiniles.scheduledjob.scheduler.PollingScheduler
import io.ktor.server.application.*

fun Application.module(applicationContext: ApplicationContext = ApplicationContext(this)) {
    displayBanner()
    configureSerialization()
    configureMonitoring()
    configureRouting()
    configureFirebase()
    configureScheduledJob(
        applicationContext.libreClient,
        applicationContext.firebaseClient,
        applicationContext.jobScheduler
    )
}

open class ApplicationContext(
    val application: Application,
    val libreClient: LibreClient = LibreClient(application),
    val firebaseClient: FirebaseClient = FirebaseClient(),
    val jobScheduler: JobScheduler = PollingScheduler(application)
)
