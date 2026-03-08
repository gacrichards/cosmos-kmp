package com.gacrichards.cosmos.domain.model

data class EpicImage(
    val identifier: String,
    val date: String,
    val imageName: String,
    val caption: String,
    val thumbnailUrl: String,
    val fullUrl: String,
)
