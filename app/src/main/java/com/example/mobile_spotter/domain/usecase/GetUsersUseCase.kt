package com.example.mobile_spotter.domain.usecase

import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.utils.Try
import com.example.mobile_spotter.utils.attempt
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val apiService: ApiService
) : UseCase<UserList> {

    override suspend fun execute(): Try<UserList> {
        return attempt {
            apiService.getAllUsers()
        }
    }
}