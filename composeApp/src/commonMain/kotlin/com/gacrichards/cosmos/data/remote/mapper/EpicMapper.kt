package com.gacrichards.cosmos.data.remote.mapper

import com.gacrichards.cosmos.data.remote.NASA_EPIC_ARCHIVE_BASE_URL
import com.gacrichards.cosmos.data.remote.dto.EpicImageDto
import com.gacrichards.cosmos.domain.model.EpicImage

internal fun EpicImageDto.toDomain(): EpicImage {
    // date from API is "YYYY-MM-DD HH:MM:SS", extract date part
    val datePart = date.take(10)
    val (year, month, day) = datePart.split("-")
    val base = "$NASA_EPIC_ARCHIVE_BASE_URL/$year/$month/$day"
    return EpicImage(
        identifier = identifier,
        date = datePart,
        imageName = image,
        caption = caption,
        thumbnailUrl = "$base/thumbs/$image.jpg",
        fullUrl = "$base/png/$image.png",
    )
}
