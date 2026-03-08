package com.gacrichards.cosmos.data.repository

import com.gacrichards.cosmos.data.local.EpicCache
import com.gacrichards.cosmos.data.remote.EpicApiService
import com.gacrichards.cosmos.data.remote.mapper.toDomain
import com.gacrichards.cosmos.domain.model.EpicImage
import com.gacrichards.cosmos.domain.repository.EpicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class EpicRepositoryImpl(
    private val apiService: EpicApiService,
    private val cache: EpicCache,
) : EpicRepository {

    override fun getAvailableDates(): Flow<Result<List<String>>> = flow {
        val cached = cache.getAvailableDates()
        if (cached.isNotEmpty()) emit(Result.success(cached))

        try {
            val fresh = apiService.getAvailableDates()
            cache.insertAvailableDates(fresh)
            emit(Result.success(fresh))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(Result.failure(e))
        }
    }

    override fun getImages(date: String): Flow<Result<List<EpicImage>>> = flow {
        val cached = cache.getImagesByDate(date)
        if (cached.isNotEmpty()) emit(Result.success(cached))

        try {
            val fresh = apiService.getImages(date).map { it.toDomain() }
            cache.insertImages(fresh)
            emit(Result.success(fresh))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(Result.failure(e))
        }
    }
}
