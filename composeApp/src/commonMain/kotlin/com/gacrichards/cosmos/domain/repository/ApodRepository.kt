package com.gacrichards.cosmos.domain.repository

import com.gacrichards.cosmos.domain.model.Apod
import kotlinx.coroutines.flow.Flow

interface ApodRepository {
    fun getTodayApod(): Flow<Result<Apod>>
    fun getApodByDate(date: String): Flow<Result<Apod>>
    fun getApodArchive(startDate: String, endDate: String): Flow<Result<List<Apod>>>
}
