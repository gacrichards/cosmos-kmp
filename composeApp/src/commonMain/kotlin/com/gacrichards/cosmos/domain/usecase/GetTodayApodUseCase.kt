package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.model.Apod
import com.gacrichards.cosmos.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow

class GetTodayApodUseCase(private val repository: ApodRepository) {
    operator fun invoke(): Flow<Result<Apod>> = repository.getTodayApod()
}
