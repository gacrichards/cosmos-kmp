package com.gacrichards.cosmos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApodDto(
    val date: String,
    val title: String,
    val explanation: String,
    @SerialName("media_type") val mediaType: MediaTypeDto = MediaTypeDto.UNKNOWN,
    val url: String,
    @SerialName("hdurl") val hdUrl: String? = null,
    val copyright: String? = null,
)
