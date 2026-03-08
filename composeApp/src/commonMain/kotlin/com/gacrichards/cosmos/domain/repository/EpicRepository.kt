package com.gacrichards.cosmos.domain.repository

import com.gacrichards.cosmos.domain.model.EpicImage
import kotlinx.coroutines.flow.Flow

interface EpicRepository {
    fun getAvailableDates(): Flow<Result<List<String>>>
    fun getImages(date: String): Flow<Result<List<EpicImage>>>
}
