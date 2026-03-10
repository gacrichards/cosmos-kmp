package com.gacrichards.cosmos.data.repository

import com.gacrichards.cosmos.data.local.ApodCache
import com.gacrichards.cosmos.data.remote.ApodApiService
import com.gacrichards.cosmos.data.remote.mapper.toDomain
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ApodRepositoryImpl(
    private val apiService: ApodApiService,
    private val cache: ApodCache,
) : ApodRepository {

    override fun getTodayApod(): Flow<Result<Apod>> = flow {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        cache.getByDate(today)?.let { emit(Result.success(it)) }
        try {
            val fresh = apiService.getTodayApod().toDomain()
            cache.insert(fresh)
            emit(Result.success(fresh))
        } catch (e: Exception) {
            if (cache.getByDate(today) == null) emit(Result.failure(e))
        }
    }

    override fun getApodByDate(date: String): Flow<Result<Apod>> = flow {
        cache.getByDate(date)?.let { emit(Result.success(it)) }
        try {
            val fresh = apiService.getApodByDate(date).toDomain()
            cache.insert(fresh)
            emit(Result.success(fresh))
        } catch (e: Exception) {
            if (cache.getByDate(date) == null) emit(Result.failure(e))
        }
    }

    override fun getApodArchive(startDate: String, endDate: String): Flow<Result<List<Apod>>> = flow {
        try {
            val apods = apiService.getApodArchive(startDate, endDate).map { it.toDomain() }
            apods.forEach { cache.insert(it) }
            emit(Result.success(apods.sortedByDescending { it.date }))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
