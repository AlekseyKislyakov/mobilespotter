package com.example.mobile_spotter.data.remote

import com.example.mobile_spotter.data.entities.DeviceList
import com.example.mobile_spotter.data.entities.UserList
import retrofit2.http.GET
import retrofit2.http.Url

const val ENDPOINT = "http://stat.handh.ru:9898/"

interface ApiService {

    // TODO remove token
    @GET("users?token=keddva5rd")
    suspend fun getAllUsers() : UserList

    @GET("http://location.handh.ru:8877/device?token=test")
    suspend fun getAllDevices() : DeviceList

}
