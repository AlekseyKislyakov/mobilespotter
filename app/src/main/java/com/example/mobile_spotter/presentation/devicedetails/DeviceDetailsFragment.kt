package com.example.mobile_spotter.presentation.devicedetails

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
class DeviceDetailsFragment : BaseFragment(R.layout.fragment_device_details) {

    private val viewModel by viewModels<DeviceDetailsViewModel>()

    @Inject
    lateinit var navigator: AppNavigator

    private var refreshFilters = false

    override val showBottomNavigationView = true

    // initial value set to 180 because image needs to be inverted
    private var angle = 180f
    private var filtersExpanded = false

    override fun callOperations() {

    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_userlist)

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
//        if (value != -1) {
//            hideBottomNavigation()
//        } else {
//            GlobalScope.launch(context = Dispatchers.Main) {
//                delay(50)
//                showBottomNavigation()
//            }
//        }
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