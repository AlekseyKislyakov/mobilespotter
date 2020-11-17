package com.example.mobile_spotter.domain.usecase

import com.example.mobile_spotter.data.entities.DeviceList
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.data.remote.DEVICE_TOKEN
import com.example.mobile_spotter.utils.Try
import com.example.mobile_spotter.utils.attempt
import javax.inject.Inject

class GetDevicesUseCase @Inject constructor(
    private val apiService: ApiService
) : UseCase<DeviceList> {

    override suspend fun execute(): Try<DeviceList> {
        return attempt {
            apiService.getAllDevices(DEVICE_TOKEN)
        }
    }
}