package com.gacrichards.cosmos.domain.repository

import com.gacrichards.cosmos.domain.model.Apod
import kotlinx.coroutines.flow.Flow

interface ApodRepository {
    fun getTodayApod(): Flow<Result<Apod>>
}
