package com.example.mobile_spotter.domain.usecase

import com.example.mobile_spotter.data.entities.request.EditDeviceRequest
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.data.remote.DEVICE_TOKEN
import com.example.mobile_spotter.utils.Try
import com.example.mobile_spotter.utils.attempt
import javax.inject.Inject

class ReturnDeviceUseCase @Inject constructor(
    private val apiService: ApiService
) : UseCaseWithRequestParams<Int, Unit> {

    override suspend fun execute(requestValues: Int): Try<Unit> {
        return attempt {
            apiService.editDeviceStatus(DEVICE_TOKEN, EditDeviceRequest(requestValues))
        }
    }
}