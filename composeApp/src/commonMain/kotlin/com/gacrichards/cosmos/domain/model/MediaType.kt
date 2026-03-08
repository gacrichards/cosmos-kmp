package com.gacrichards.cosmos.domain.model

sealed class MediaType {
    data class Image(val url: String) : MediaType()
    data class Video(val url: String) : MediaType()
}
