package com.gacrichards.cosmos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpicImageDto(
    val identifier: String,
    val caption: String,
    val image: String,
    val date: String,
)

@Serializable
data class EpicAvailableDateDto(
    val date: String,
)
