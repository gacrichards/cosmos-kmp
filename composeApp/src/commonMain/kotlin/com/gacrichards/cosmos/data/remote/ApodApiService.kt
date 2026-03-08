package com.gacrichards.cosmos.data.remote

import com.gacrichards.cosmos.data.remote.dto.ApodDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ApodApiService(
    private val client: HttpClient,
    private val apiKey: String,
) {
    suspend fun getTodayApod(): ApodDto =
        client.get("$NASA_BASE_URL$NASA_APOD_PATH") {
            parameter(NASA_PARAM_API_KEY, apiKey)
        }.body()
}
