package com.example.mobile_spotter.presentation.devicelist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.domain.usecase.GetDevicesUseCase
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.ext.detailedResolution
import com.example.mobile_spotter.ext.detailedVersion
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.flow.collect
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

    val filterParameters = MutableLiveData(DeviceFilter())
    var initialRequest = true

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
        refreshFilters.value = Unit
        filterParameters.value = filterParameters.value?.copy(
            os = type,
            selectedResolutionSet = filterParameters.value?.resolutionSet ?: mutableSetOf(),
            selectedVersionSet = filterParameters.value?.versionSet ?: mutableSetOf()
        )
    }

    fun setOsList(versions: MutableSet<String>) {
        filterParameters.value = filterParameters.value?.copy(selectedVersionSet = versions)
    }

    fun setResolutionList(resolutions: MutableSet<String>) {
        filterParameters.value = filterParameters.value?.copy(selectedResolutionSet = resolutions)
    }

    fun setPrivateType(isGeneral: Boolean) {
        filterParameters.value = filterParameters.value?.copy(nonPrivate = isGeneral)
    }

    fun setFreeType(isAvailable: Boolean) {
        filterParameters.value = filterParameters.value?.copy(onlyAvailable = isAvailable)
    }

    private fun makeDevicesRequest() {
        viewModelScope.launch {
            progressive {
                getDevicesUseCase.execute()
            }.collect {
                getDevicesOperation.value = it
                it.doOnSuccess {
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
                    if (initialRequest) {
                        fillFilterValue()
                        initialRequest = false
                    }
                }
            }
        }
    }

    private fun fillFilterValue() {
        filterParameters.value = DeviceFilter(
            os = OS_ALL,
            versionSet = deviceListLiveData.value?.map { it.detailedVersion() }?.toMutableSet() ?: mutableSetOf(),
            selectedVersionSet = deviceListLiveData.value?.map { it.detailedVersion() }?.toMutableSet()
                ?: mutableSetOf(),
            resolutionSet = deviceListLiveData.value?.map { it.detailedResolution() }?.toMutableSet() ?: mutableSetOf(),
            selectedResolutionSet = deviceListLiveData.value?.map { it.detailedResolution() }?.toMutableSet()
                ?: mutableSetOf()
        )
    }
}