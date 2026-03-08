package com.gacrichards.cosmos.data.remote

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
