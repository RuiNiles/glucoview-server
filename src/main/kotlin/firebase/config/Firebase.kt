package com.ruiniles.firebase.config

import com.google.auth.oauth2.GoogleCredentials.fromStream
import com.google.firebase.FirebaseApp.initializeApp
import com.google.firebase.FirebaseOptions
import com.ruiniles.getProperty
import io.ktor.server.application.*
import java.io.File

fun Application.configureFirebase() {
    val serviceAccountJsonPath = getProperty("FIREBASE_SERVICE_ACCOUNT_JSON_PATH")

    val credentials = fromStream(serviceAccountJsonPath.let { File(it).inputStream() })

    val options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build()

    initializeApp(options)
}

