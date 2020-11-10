package com.example.mobile_spotter.presentation.devicelist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.domain.usecase.GetDevicesUseCase
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.ext.detailedResolution
import com.example.mobile_spotter.ext.detailedVersion
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.*
import com.example.mobile_spotter.utils.Failure
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class DeviceListViewModel @ViewModelInject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val preferencesStorage: PreferencesStorage
) : BaseViewModel() {
    val getDevicesOperation = MutableLiveData<LongOperation<DeviceList>>()
    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()

    val queryLiveData = MutableLiveData<String>()
    val applyUser = MutableLiveData<User>()

    val applyDevice = MutableLiveData<Device>()

    val refreshFilters = MutableLiveData<Unit>()

    val deviceListLiveData = MutableLiveData<List<Device>>()

    var filterParameters = DeviceFilter()
    var initialRequest = true

    val userList = mutableListOf<User>()
    val deviceList = mutableListOf<Device>()

    fun getDevices() {
        makeDevicesRequest()
    }

    fun setQuery(query: CharSequence) {
        queryLiveData.value = query.toString()
    }

    fun selectUser(user: User) {
        applyUser.value = user
    }

    fun setDevice(device: Device) {
        preferencesStorage.deviceId = device.id
        applyDevice.value = device
    }

    fun setOsType(type: String) {
        filterParameters.apply {
            os = type
            selectedResolutionSet = resolutionSet.toMutableSet()
            selectedVersionSet = versionSet.toMutableSet()
        }
        refreshFilters.value = Unit
    }

    fun setOsList(versions: MutableSet<String>) {
        filterParameters.apply {
            selectedVersionSet = versions
        }
        refreshFilters.value = Unit
    }

    fun setResolutionList(resolutions: MutableSet<String>) {
        filterParameters.apply {
            selectedResolutionSet = resolutions
        }
        refreshFilters.value = Unit
    }

    fun setPrivateType(isGeneral: Boolean) {
        filterParameters.apply {
            nonPrivate = isGeneral
        }
        refreshFilters.value = Unit
    }

    fun setFreeType(isAvailable: Boolean) {
        filterParameters.apply {
            onlyAvailable = isAvailable
        }
        refreshFilters.value = Unit
    }

    fun handleCode(code: String): Any? {
        val entity = userList.firstOrNull { it.rfid == code } ?: deviceList.firstOrNull { it.tokenUid == code }
        entity?.let {
            when (it) {
                is User -> {
                    preferencesStorage.userId = it.id
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
                getDevicesOperation.postValue(it)
                it.doOnSuccess {
                    deviceList.clear()
                    deviceList.addAll(it)

                    deviceListLiveData.postValue(it)
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

                    if (initialRequest) {
                        fillFilterValue()
                        initialRequest = false
                    }
                }
            }
        }
    }

    private fun fillFilterValue() {
        filterParameters = DeviceFilter(
                os = OS_ALL,
                versionSet = deviceListLiveData.value?.map { it.detailedVersion() }?.toMutableSet() ?: mutableSetOf(),
                selectedVersionSet = deviceListLiveData.value?.map { it.detailedVersion() }?.toMutableSet()
                        ?: mutableSetOf(),
                resolutionSet = deviceListLiveData.value?.map { it.detailedResolution() }?.toMutableSet() ?: mutableSetOf(),
                selectedResolutionSet = deviceListLiveData.value?.map { it.detailedResolution() }?.toMutableSet()
                        ?: mutableSetOf()
        )
        refreshFilters.value = Unit
    }
}