package com.ruiniles

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalDateTime.Companion.Format
import kotlinx.datetime.format.Padding.NONE
import kotlinx.datetime.format.char
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest.getInstance

class LibreClient(application: Application) {
    val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    val userId = application.getProperty("USER_ID")
    val apiToken = application.getProperty("API_TOKEN")
    val libreViewBaseUrl = application.getProperty("LIBRE_VIEW_BASE_URL")

    suspend fun fetchGraph(): BgReading {
        val endpoint = "/llu/connections/$userId/graph"
        val url = "$libreViewBaseUrl$endpoint"

        val response: GetGraphResponse = client.get(url) {
            headers {
                append(Accept, "application/json")
                append(ContentType, "application/json")
                append(Authorization, "Bearer $apiToken")
                append("product", "llu.android")
                append("version", "4.16")
                append("account-id", userId.sha256Hex())
            }
        }.body()

        val glucoseMeasurement = response.data!!.connection.glucoseMeasurement
        val graphData = response.data.graphData
        return BgReading(
            glucoseMeasurement.timestamp.toLocalDateTime(),
            glucoseMeasurement.trendArrow,
            BgReading.Level(
                glucoseMeasurement.value,
                glucoseMeasurement.valueInMgPerDl
            ),
            graphData.map { dataPoint ->
                BgReading.Data(
                    dataPoint.timestamp.toLocalDateTime(),
                    BgReading.Level(
                        dataPoint.value,
                        dataPoint.valueInMgPerDl
                    )
                )
            }
        )
    }
}

@Serializable
data class GetGraphResponse(
    val status: Int,
    val data: Data? = null
) {
    @Serializable
    data class Data(
        val connection: Connection,
        val graphData: List<GraphData>,
    )

    @Serializable
    data class Connection(
        val glucoseMeasurement: GlucoseMeasurement
    ) {
        @Serializable
        data class GlucoseMeasurement(
            @SerialName("Timestamp") val timestamp: String,
            @SerialName("TrendArrow") val trendArrow: Int,
            @SerialName("ValueInMgPerDl") val valueInMgPerDl: Int,
            @SerialName("Value") val value: Float,
        )
    }

    @Serializable
    data class GraphData(
        @SerialName("Timestamp") val timestamp: String,
        @SerialName("Value") val value: Float,
        @SerialName("ValueInMgPerDl") val valueInMgPerDl: Int,
    )
}

@Serializable
data class BgReading(
    val timestamp: LocalDateTime,
    val trend: Int,
    val value: Level,
    val graphData: List<Data>
) {
    @Serializable
    data class Level(val mmolPerL: Float, val mgPerDl: Int)

    @Serializable
    data class Data(val timestamp: LocalDateTime, val value: Level)
}

private fun String.sha256Hex() = this
    .toByteArray()
    .let { bytes ->
        getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

private fun String.toLocalDateTime(): LocalDateTime =
    LocalDateTime.parse(this, Format {
        monthNumber(padding = NONE)
        char('/')
        day(padding = NONE)
        char('/')
        year(padding = NONE)
        char(' ')
        amPmHour(padding = NONE)
        char(':')
        minute(padding = NONE)
        char(':')
        second(padding = NONE)
        char(' ')
        amPmMarker("AM", "PM")
    })