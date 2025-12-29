package com.ruiniles.scheduledjob.scheduler

import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.time.delay
import java.time.Duration.ofSeconds

class PollingScheduler(val application: Application) : JobScheduler() {
    override fun start() {
        val pollingScope = CoroutineScope(SupervisorJob() + IO)

        val pollingJob = pollingScope.launch {
            while (isActive) {
                job.run()
                delay(ofSeconds(10))
            }
        }

        application.monitor.subscribe(ApplicationStopped) {
            pollingJob.cancel()
            runBlocking { pollingJob.join() }
        }
    }
}
