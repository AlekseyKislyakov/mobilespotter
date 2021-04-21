package com.example.mobile_spotter.presentation.userlist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_spotter.data.entities.Device
import com.example.mobile_spotter.data.entities.User
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.data.preferences.PreferencesStorage
import com.example.mobile_spotter.domain.usecase.GetUsersUseCase
import com.example.mobile_spotter.presentation.base.BaseViewModel
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.progressive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val preferences: PreferencesStorage
) : BaseViewModel() {

    val getUsersOperation = MutableLiveData<LongOperation<UserList>>()
    val queryLiveData = MutableLiveData<String>()
    val applyUser = MutableLiveData<User>()

    val userList = mutableListOf<User>()

    fun getUsers() {
        makeRequest()
    }

    fun setQuery(query: CharSequence) {
        queryLiveData.value = query.toString()
    }

    fun selectUser(user: User) {
        preferences.userId = user.id
        applyUser.value = user
    }

    fun handleCode(code: String): Any? {
        val entity = userList.firstOrNull { it.rfid == code }
        entity?.let {
            selectUser(it)
        }
        return entity
    }

    private fun makeRequest() {
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
