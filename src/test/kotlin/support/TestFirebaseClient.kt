package com.ruiniles.support

import com.ruiniles.BgReading
import com.ruiniles.firebase.client.FirebaseClient

class TestFirebaseClient : FirebaseClient() {
    val readings: MutableList<BgReading> = mutableListOf()

    override fun sendBgReadingToDevice(bgReading: BgReading) {
        readings.add(bgReading)
    }
}
