package com.ruiniles.support

import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.google.firebase.FirebaseApp
import com.ruiniles.module
import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertEquals

interface WithGlucoviewServer {
    fun serverTest(
        vararg overrides: Pair<String, String>,
        testBlock: suspend ApplicationTestBuilder.(testAppContext: TestApplicationContext) -> Unit
    ) {
        lateinit var testAppContext: TestApplicationContext
        val testJobScheduler = TestJobScheduler()
        val testFirebaseClient = TestFirebaseClient()

        testApplication {
            environment {
                config = testConfig(
                    mutableMapOf(
                        "USER_ID" to "default-user-id",
                        "API_TOKEN" to "default-api-token",
                        "LIBRE_VIEW_BASE_URL" to wireMock.baseUrl(),
                        "FIREBASE_SERVICE_ACCOUNT_JSON_PATH" to "src/main/resources/serviceAccountKey.json",
                    ).apply { putAll(overrides) })
                    .withFallback(HoconApplicationConfig(ConfigFactory.load().withoutPath("ktor.application")))
            }

            application {
                testAppContext = TestApplicationContext(this,
                    testJobScheduler,
                    testFirebaseClient
                )
                module(testAppContext)
            }

            appHasStarted()

            testBlock(testAppContext)
        }
    }

    @BeforeEach
    fun setup() {
        runCatching { FirebaseApp.getInstance().delete() }
    }

    @AfterEach
    fun tearDown() {
        wireMock.resetAll()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build()

        private fun testConfig(valueMap: Map<String, String>): ApplicationConfig =
            valueMap.mapKeys { (key, _) -> "ktor.env.$key" }
                .toList()
                .let { MapApplicationConfig(it) }

        suspend fun ApplicationTestBuilder.appHasStarted() {
            client.get("/").apply { assertEquals(HttpStatusCode.OK, status) }
        }
    }
}
