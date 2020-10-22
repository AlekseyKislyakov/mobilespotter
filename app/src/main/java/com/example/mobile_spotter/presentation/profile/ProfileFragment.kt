package com.example.mobile_spotter.presentation.profile

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.Device
import com.example.mobile_spotter.data.entities.User
import com.example.mobile_spotter.data.navigator.AppNavigator
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_device_details.*
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    private val viewModel by viewModels<ProfileViewModel>()

    override val showBottomNavigationView = true

    override fun callOperations() {

    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_profile)

        buttonRetry.setOnClickListener {
            makeDevicesRequest()
        }
    }

    override fun onBindViewModel() {
        makeDevicesRequest()

        observe(viewModel.getUsersOperation) {
            handleGetDevicesState(it.state)
            it.doOnSuccess { userInfo ->
                handleInfo(userInfo, viewModel.deviceListLiveData.value ?: emptyList())
            }
        }

        observe(viewModel.getDevicesOperation) {
            handleGetDevicesState(it.state)
        }

    }

    override fun onKeyboardHeightChanged(value: Int) {

    }

    private fun handleGetDevicesState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                viewLoading.isVisible = true
                buttonRetry.isGone = true
                // recyclerViewDevices.isVisible = false
            }
            OpState.SUCCESS -> {
                viewLoading.isVisible = false
                buttonRetry.isGone = true
                // recyclerViewDevices.isVisible = true
            }
            OpState.FAILURE -> {
                viewLoading.isVisible = false
                buttonRetry.isVisible = true
                // recyclerViewDevices.isVisible = false
            }
        }
    }

    private fun makeDevicesRequest() {
        viewModel.getDevices()
    }

    private fun handleInfo(userList: List<User>, deviceList: List<Device>) {
    }

}