package com.example.mobile_spotter.domain.usecase

import com.example.mobile_spotter.utils.Try

interface UseCase<T> {
    suspend fun execute(): Try<T>
}

