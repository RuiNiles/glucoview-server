package com.ruiniles.support

import com.ruiniles.ApplicationContext
import io.ktor.server.application.*

class TestApplicationContext(
    application: Application,
    val testJobScheduler: TestJobScheduler,
    val testFirebaseClient: TestFirebaseClient
) :
    ApplicationContext(
        application,
        jobScheduler = testJobScheduler,
        firebaseClient = testFirebaseClient
    )
