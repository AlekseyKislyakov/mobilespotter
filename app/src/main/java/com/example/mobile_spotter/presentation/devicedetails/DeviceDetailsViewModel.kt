package com.example.mobile_spotter.presentation.devicedetails

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.data.entities.request.EditDeviceRequest
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.domain.usecase.TakeDeviceUseCase
import com.example.mobile_spotter.domain.usecase.GetDevicesUseCase
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.domain.usecase.ReturnDeviceUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DeviceDetailsViewModel @ViewModelInject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val takeDeviceUseCase: TakeDeviceUseCase,
    private val returnDeviceUseCase: ReturnDeviceUseCase,
    private val preferencesStorage: PreferencesStorage
) : BaseViewModel() {
    val getDevicesOperation = MutableLiveData<LongOperation<DeviceList>>()
    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()

    val deviceListLiveData = MutableLiveData<List<Device>>()

    val takeDeviceLiveData = MutableLiveData<LongOperation<Unit>>()
    val returnDeviceLiveData = MutableLiveData<LongOperation<Unit>>()

    val newUserId = MutableLiveData<String>()

    val userList = mutableListOf<User>()
    val deviceList = mutableListOf<Device>()

    var originId: Int?
        get() = preferencesStorage.userId
        set(value) {
            preferencesStorage.userId = value
        }

    val isPublic: Boolean?
        get() = preferencesStorage.publicAccount

    fun getDevices() {
        makeDevicesRequest()
    }

    fun takeDevice() {
        viewModelScope.launch {
            progressive {
                takeDeviceUseCase.execute(EditDeviceRequest(preferencesStorage.deviceId ?: 0, preferencesStorage.userId ?: 0))
            }.collect {
                takeDeviceLiveData.value = it
            }
        }
    }

    fun returnDevice() {
        viewModelScope.launch {
            progressive {
                returnDeviceUseCase.execute(preferencesStorage.deviceId ?: 0)
            }.collect {
                returnDeviceLiveData.value = it
            }
        }
    }

    fun handleCode(code: String): Any? {
        val entity = userList.firstOrNull { it.rfid == code } ?: deviceList.firstOrNull { it.tokenUid == code }
        entity?.let {
            when (it) {
                is User -> {
                    originId = it.id
                }
                is Device -> {
                    preferencesStorage.deviceId = it.id
                }
            }
        }
        return entity
    }

    private fun makeDevicesRequest() {
        viewModelScope.launch {
            progressive {
                getDevicesUseCase.execute()
            }.collect {
                getDevicesOperation.value = it
                it.doOnSuccess {
                    deviceList.clear()
                    deviceList.addAll(it)
                    deviceListLiveData.value = it
                    makeUsersRequest()
                }
            }
        }
    }

    private fun makeUsersRequest() {
        viewModelScope.launch {
            progressive {
                getUsersUseCase.execute()
            }.collect {
                getUsersOperation.value = it
                it.doOnSuccess {
                    userList.clear()
                    userList.addAll(it)
                }
            }
        }
    }
}