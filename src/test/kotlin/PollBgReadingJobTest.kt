package com.ruiniles

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.ruiniles.BgReading.Level
import com.ruiniles.GetGraphResponse.Connection
import com.ruiniles.GetGraphResponse.Connection.GlucoseMeasurement
import com.ruiniles.GetGraphResponse.Data
import com.ruiniles.support.WithGlucoviewServer
import com.ruiniles.support.WithGlucoviewServer.Companion.wireMock
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import java.time.LocalDateTime.of
import kotlin.test.Test

class PollBgReadingJobTest : WithGlucoviewServer {

    @Test
    fun pollsReadingAndSendsFirebaseMessage() = serverTest("USER_ID" to "some-user-id") { appContext ->
        wireMock.stubFor(
            get(urlEqualTo("/llu/connections/some-user-id/graph"))
                .willReturn(
                    okJson(
                        Json.encodeToString(
                            GetGraphResponse(
                                0,
                                Data(
                                    Connection(
                                        GlucoseMeasurement(
                                            "10/5/2025 5:23:50 PM",
                                            3,
                                            91,
                                            5.1f
                                        )
                                    ),
                                    emptyList()
                                )
                            )
                        )
                    )
                )
        )

        appContext.testJobScheduler.runJob()

        assertThat(
            appContext.testFirebaseClient.readings,
            contains(
                BgReading(
                    of(2025, 10, 5, 17, 23, 50).toKotlinLocalDateTime(),
                    3,
                    Level(5.1f, 91),
                    emptyList()
                )
            )
        )

        wireMock.verify(
            exactly(1),
            getRequestedFor(urlEqualTo("/llu/connections/some-user-id/graph"))
        )
    }

    @Test
    fun sendsFirebaseMessageOnNewReading() = serverTest("USER_ID" to "some-user-id") { appContext ->
        wireMock.stubFor(
            get(urlEqualTo("/llu/connections/some-user-id/graph"))
                .inScenario("multiple readings")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("SECOND")
                .willReturn(
                    okJson(
                        Json.encodeToString(
                            GetGraphResponse(
                                0,
                                Data(
                                    Connection(
                                        GlucoseMeasurement(
                                            "10/5/2025 5:23:50 PM",
                                            3,
                                            91,
                                            5.1f
                                        )
                                    ),
                                    emptyList()
                                )
                            )
                        )
                    )
                )
        )

        wireMock.stubFor(
            get(urlEqualTo("/llu/connections/some-user-id/graph"))
                .inScenario("multiple readings")
                .whenScenarioStateIs("SECOND")
                .willReturn(
                    okJson(
                        Json.encodeToString(
                            GetGraphResponse(
                                0,
                                Data(
                                    Connection(
                                        GlucoseMeasurement(
                                            "10/5/2025 5:23:51 PM",
                                            3,
                                            91,
                                            6.1f
                                        )
                                    ),
                                    emptyList()
                                )
                            )
                        )
                    )
                )
        )

        appContext.testJobScheduler.runJob()
        appContext.testJobScheduler.runJob()

        assertThat(
            appContext.testFirebaseClient.readings,
            contains(
                BgReading(
                    of(2025, 10, 5, 17, 23, 50).toKotlinLocalDateTime(),
                    3,
                    Level(5.1f, 91),
                    emptyList()
                ),
                BgReading(
                    of(2025, 10, 5, 17, 23, 51).toKotlinLocalDateTime(),
                    3,
                    Level(6.1f, 91),
                    emptyList()
                )
            )
        )

        wireMock.verify(
            exactly(2),
            getRequestedFor(urlEqualTo("/llu/connections/some-user-id/graph"))
        )
    }

    @Test
    fun doesNotSendFirebaseMessageIfReadingIsUnchanged() = serverTest("USER_ID" to "some-user-id") { appContext ->
        wireMock.stubFor(
            get(urlEqualTo("/llu/connections/some-user-id/graph"))
                .willReturn(
                    okJson(
                        Json.encodeToString(
                            GetGraphResponse(
                                0,
                                Data(
                                    Connection(
                                        GlucoseMeasurement(
                                            "10/5/2025 5:23:50 PM",
                                            3,
                                            91,
                                            5.1f
                                        )
                                    ),
                                    emptyList()
                                )
                            )
                        )
                    )
                )
        )

        appContext.testJobScheduler.runJob()
        appContext.testJobScheduler.runJob()

        assertThat(
            appContext.testFirebaseClient.readings,
            contains(
                BgReading(
                    of(2025, 10, 5, 17, 23, 50).toKotlinLocalDateTime(),
                    3,
                    Level(5.1f, 91),
                    emptyList()
                )
            )
        )

        wireMock.verify(
            exactly(2),
            getRequestedFor(urlEqualTo("/llu/connections/some-user-id/graph"))
        )
    }
}
