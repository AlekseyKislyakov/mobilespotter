package com.example.mobile_spotter.presentation.devicelist

import androidx.hilt.lifecycle.ViewModelInject
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel

class DeviceListViewModel @ViewModelInject constructor(
    private val getDevicesUseCase: GetUsersUseCase
) : BaseViewModel() {

}