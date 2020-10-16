package com.example.mobile_spotter.presentation.userlist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.User
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserListViewModel @ViewModelInject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : BaseViewModel() {

    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()
    val queryLiveData = MutableLiveData<String>()
    val applyUser = MutableLiveData<User>()

    fun getUsers() {
        makeRequest()
    }

    fun setQuery(query: CharSequence) {
        queryLiveData.value = query.toString()
    }

    fun selectUser(user: User) {
        applyUser.value = user
    }

    private fun makeRequest() {
        viewModelScope.launch {
            progressive {
                getUsersUseCase.execute()
            }.collect {
                getUsersOperation.value = it
            }
        }
    }
}
