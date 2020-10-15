package com.example.mobile_spotter.presentation.userlist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.LoadableResult
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.data.remote.ApiService
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.presentation.base.SingleLiveEvent
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListViewModel @ViewModelInject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : BaseViewModel() {

    val liveData = MutableLiveData<LongOperation<UserList>>()

    fun getUsers() {
        makeRequest()
    }

    private fun makeRequest() {
        viewModelScope.launch {
            progressive {
                getUsersUseCase.execute()
            }.collect {
                liveData.value = it
            }
        }
    }
}
