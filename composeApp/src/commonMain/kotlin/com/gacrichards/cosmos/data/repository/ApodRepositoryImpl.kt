package com.gacrichards.cosmos.data.repository

import com.gacrichards.cosmos.data.remote.ApodApiService
import com.gacrichards.cosmos.data.remote.mapper.toDomain
import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApodRepositoryImpl(
    private val apiService: ApodApiService,
) : ApodRepository {

    override fun getTodayApod(): Flow<Result<Apod>> = flow {
        try {
            val apod = apiService.getTodayApod().toDomain()
            emit(Result.success(apod))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
