package com.example.mobile_spotter.presentation.devicelist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.domain.usecase.GetDevicesUseCase
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DeviceListViewModel @ViewModelInject constructor(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : BaseViewModel() {
    val getDevicesOperation = MutableLiveData<LongOperation<DeviceList>>()
    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()
    
    val queryLiveData = MutableLiveData<String>()
    val applyUser = MutableLiveData<User>()
    
    val deviceListLiveData = MutableLiveData<List<Device>>()
    
    val filterParameters = MutableLiveData(DeviceFilter())

    fun getDevices() {
        makeDevicesRequest()
    }

    fun setQuery(query: CharSequence) {
        queryLiveData.value = query.toString()
    }

    fun selectUser(user: User) {
        applyUser.value = user
    }
    
    fun setOsType(type: String) {
        filterParameters.value = filterParameters.value?.copy(os = type)
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
            }
        }
    }
}