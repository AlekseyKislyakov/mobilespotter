package com.example.mobile_spotter.presentation.settings

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.Device
import com.example.mobile_spotter.data.entities.DeviceList
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.data.entities.request.EditDeviceRequest
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.domain.usecase.GetDevicesUseCase
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.domain.usecase.ReturnDeviceUseCase
import com.example.mobile_spotter.domain.usecase.TakeDeviceUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SettingsViewModel @ViewModelInject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val preferencesStorage: PreferencesStorage
) : BaseViewModel() {

    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()

    val token: String

    private var publicAccount = preferencesStorage.publicAccount

    fun getUsers() {
        makeUsersRequest()
    }

    fun getCurrentUserId(): Int {
        return preferencesStorage.userId ?: -1
    }

    fun getPublicAccountValue() : Boolean {
        return publicAccount ?: false
    }

//    fun getToken(): String {
//        return preferencesStorage.token ?: ""
//    }
//
//    fun setToken(token: String): String {
//        return preferencesStorage.token ?: ""
//    }

    fun setPublicAccountValue(isPublic: Boolean) {
        preferencesStorage.publicAccount = isPublic
        publicAccount = isPublic
    }

    private fun makeUsersRequest() {
        viewModelScope.launch {
            progressive {
                getUsersUseCase.execute()
            }.collect {
                getUsersOperation.value = it
            }
        }
    }
}