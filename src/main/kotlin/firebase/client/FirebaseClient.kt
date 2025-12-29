package com.ruiniles.firebase.client

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidConfig.Priority.HIGH
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.ruiniles.BgReading
import kotlinx.serialization.json.Json

open class FirebaseClient {
    open fun sendBgReadingToDevice(bgReading: BgReading) {
        val message = Message.builder()
            .putData("bgReading", Json.encodeToString(bgReading))
            .setAndroidConfig(AndroidConfig.builder().setPriority(HIGH).build())
            .setTopic("glucose-update-topic")
            .build()

        FirebaseMessaging.getInstance().send(message)
    }
}