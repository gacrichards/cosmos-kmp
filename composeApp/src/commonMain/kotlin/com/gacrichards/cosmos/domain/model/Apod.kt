package com.gacrichards.cosmos.domain.model

data class Apod(
    val date: String,
    val title: String,
    val explanation: String,
    val mediaType: MediaType,
    val hdUrl: String?,
    val copyright: String?,
)
