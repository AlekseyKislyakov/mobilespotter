package com.example.mobile_spotter.domain.usecase

import com.example.mobile_spotter.utils.Try

interface UseCaseWithRequestParams<In, Out> {
    suspend fun execute(requestValues: In): Try<Out>
}