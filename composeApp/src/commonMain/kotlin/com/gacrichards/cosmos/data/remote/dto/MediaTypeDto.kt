package com.gacrichards.cosmos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaTypeDto {
    @SerialName("image") IMAGE,
    @SerialName("video") VIDEO,
    UNKNOWN, // fallback for unrecognized values via coerceInputValues = true
}
