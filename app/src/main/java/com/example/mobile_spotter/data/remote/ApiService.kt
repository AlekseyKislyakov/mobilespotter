package com.example.mobile_spotter.data.remote

import com.example.mobile_spotter.data.entities.DeviceList
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.data.entities.request.EditDeviceRequest
import retrofit2.http.*

const val ENDPOINT = "http://stat.handh.ru:9898/"

interface ApiService {

    // TODO remove token
    @GET("users")
    suspend fun getAllUsers(@Query("token") token: String) : UserList

    @GET("http://location.handh.ru:8877/device?token=test")
    suspend fun getAllDevices() : DeviceList

    @POST("http://location.handh.ru:8877/owner?token=test")
    suspend fun editDeviceStatus(@Body editDevice: EditDeviceRequest)

}
