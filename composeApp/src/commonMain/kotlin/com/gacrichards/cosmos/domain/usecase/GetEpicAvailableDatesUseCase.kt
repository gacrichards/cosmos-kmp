package com.gacrichards.cosmos.domain.usecase

import com.gacrichards.cosmos.domain.repository.EpicRepository
import kotlinx.coroutines.flow.Flow

class GetEpicAvailableDatesUseCase(private val repository: EpicRepository) {
    operator fun invoke(): Flow<Result<List<String>>> =
        repository.getAvailableDates()
}
