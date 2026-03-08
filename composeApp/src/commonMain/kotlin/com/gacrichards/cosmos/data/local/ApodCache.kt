package com.gacrichards.cosmos.data.local

import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.model.MediaType

class ApodCache(private val db: CosmosDatabase) {

    fun getByDate(date: String): Apod? =
        db.cosmosDatabaseQueries.getApodByDate(date).executeAsOneOrNull()?.toDomain()

    fun insert(apod: Apod) {
        val (mediaTypeStr, url) = when (val m = apod.mediaType) {
            is MediaType.Image -> "image" to m.url
            is MediaType.Video -> "video" to m.url
        }
        db.cosmosDatabaseQueries.insertApod(
            date = apod.date,
            title = apod.title,
            explanation = apod.explanation,
            media_type = mediaTypeStr,
            url = url,
            hd_url = apod.hdUrl,
            copyright = apod.copyright,
        )
    }
}

private fun Apod_cache.toDomain(): Apod = Apod(
    date = date,
    title = title,
    explanation = explanation,
    mediaType = when (media_type) {
        "image" -> MediaType.Image(url)
        "video" -> MediaType.Video(url)
        else -> throw IllegalStateException("Unknown media_type in cache: $media_type")
    },
    hdUrl = hd_url,
    copyright = copyright,
)
