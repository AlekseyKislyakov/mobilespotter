package com.example.mobile_spotter.presentation.devicelist

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.example.mobile_spotter.R
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.presentation.userlist.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_devicelist) {

    private val viewModel by viewModels<DeviceListViewModel>()

    override fun callOperations() {
        TODO("Not yet implemented")
    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun onBindViewModel() {
        TODO("Not yet implemented")
    }

}