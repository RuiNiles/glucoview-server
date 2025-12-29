package com.ruiniles

import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = InternalServerError)
        }
    }
    routing {
        get("/") {
            call.respondText("""
                Glucoview Server
                
                version: ${getProperty("APP_VERSION")}
                
                status: OK
            """.trimIndent())
        }
    }
}
