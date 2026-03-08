package com.gacrichards.cosmos.data.local

import com.gacrichards.cosmos.domain.model.EpicImage

class EpicCache(private val db: CosmosDatabase) {

    fun getImagesByDate(date: String): List<EpicImage> =
        db.cosmosDatabaseQueries
            .getEpicImagesByDate(date)
            .executeAsList()
            .map { it.toDomain() }

    fun insertImages(images: List<EpicImage>) {
        db.cosmosDatabaseQueries.transaction {
            images.forEach { image ->
                db.cosmosDatabaseQueries.insertEpicImage(
                    identifier = image.identifier,
                    date = image.date,
                    image_name = image.imageName,
                    caption = image.caption,
                    thumbnail_url = image.thumbnailUrl,
                    full_url = image.fullUrl,
                )
            }
        }
    }

    fun getAvailableDates(): List<String> =
        db.cosmosDatabaseQueries
            .getAllAvailableDates()
            .executeAsList()

    fun insertAvailableDates(dates: List<String>) {
        db.cosmosDatabaseQueries.transaction {
            dates.forEach { date ->
                db.cosmosDatabaseQueries.insertAvailableDate(date)
            }
        }
    }
}

private fun Epic_image_cache.toDomain(): EpicImage = EpicImage(
    identifier = identifier,
    date = date,
    imageName = image_name,
    caption = caption,
    thumbnailUrl = thumbnail_url,
    fullUrl = full_url,
)
