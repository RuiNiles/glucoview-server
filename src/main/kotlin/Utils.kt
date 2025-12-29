package com.ruiniles

import io.ktor.server.application.*

fun Application.getProperty(propName: String) = environment.config
    .propertyOrNull("ktor.env.$propName")
    ?.getString()
    ?: throw IllegalStateException("$propName not set")
