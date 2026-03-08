package com.gacrichards.cosmos.data.remote

import com.gacrichards.cosmos.data.remote.dto.EpicImageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class EpicApiService(
    private val client: HttpClient,
    private val apiKey: String,
) {
    suspend fun getAvailableDates(): List<String> =
        client.get("$NASA_BASE_URL$NASA_EPIC_PATH/available") {
            parameter(NASA_PARAM_API_KEY, apiKey)
        }.body<List<String>>()

    suspend fun getImages(date: String): List<EpicImageDto> =
        client.get("$NASA_BASE_URL$NASA_EPIC_PATH/date/$date") {
            parameter(NASA_PARAM_API_KEY, apiKey)
        }.body()
}
