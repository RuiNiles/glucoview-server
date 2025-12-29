package com.ruiniles

import com.ruiniles.support.WithGlucoviewServer
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RoutingTest : WithGlucoviewServer {

    @Test
    fun root() = serverTest(
        "APP_VERSION" to "some-app-version"
    ) {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                """
                    Glucoview Server
                    
                    version: some-app-version
                    
                    status: OK
            """.trimIndent(), bodyAsText()
            )
        }
    }
}
