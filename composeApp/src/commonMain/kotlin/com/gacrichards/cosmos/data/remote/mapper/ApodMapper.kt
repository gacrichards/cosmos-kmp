package com.gacrichards.cosmos.data.remote.mapper

import com.gacrichards.cosmos.data.remote.dto.ApodDto
import com.gacrichards.cosmos.data.remote.dto.MediaTypeDto
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.model.MediaType

fun ApodDto.toDomain(): Apod = Apod(
    date = date,
    title = title,
    explanation = explanation,
    mediaType = when (mediaType) {
        MediaTypeDto.VIDEO -> MediaType.Video(url)
        MediaTypeDto.IMAGE -> MediaType.Image(url)
        MediaTypeDto.UNKNOWN -> throw IllegalStateException("Unexpected Apod media type")
    },
    hdUrl = hdUrl,
    copyright = copyright?.trim(),
)
