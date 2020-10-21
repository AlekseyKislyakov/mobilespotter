package com.example.mobile_spotter.presentation.devicedetails

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.domain.usecase.GetDevicesUseCase
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.ext.detailedResolution
import com.example.mobile_spotter.ext.detailedVersion
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class DeviceDetailsViewModel @ViewModelInject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : BaseViewModel() {
    val getDevicesOperation = MutableLiveData<LongOperation<DeviceList>>()
    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()

    val deviceListLiveData = MutableLiveData<List<Device>>()

    fun getDevices() {
        makeDevicesRequest()
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
            }
        }
    }
}