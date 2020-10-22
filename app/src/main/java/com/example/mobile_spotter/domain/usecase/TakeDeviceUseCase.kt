package com.example.mobile_spotter.domain.usecase

import com.example.mobile_spotter.data.entities.DeviceList
import com.example.mobile_spotter.data.entities.request.EditDeviceRequest
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.utils.Try
import com.example.mobile_spotter.utils.attempt
import javax.inject.Inject

class TakeDeviceUseCase @Inject constructor(
    private val apiService: ApiService
) : UseCaseWithRequestParams<EditDeviceRequest, Unit> {

    override suspend fun execute(requestValues: EditDeviceRequest): Try<Unit> {
        return attempt {
            apiService.editDeviceStatus(requestValues)
        }
    }
}