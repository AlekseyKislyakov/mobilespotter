package com.example.mobile_spotter.presentation.devicelist

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.data.entities.OS_ALL
import com.example.mobile_spotter.data.navigator.AppNavigator
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_devicelist.*
import javax.inject.Inject

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_devicelist) {

    private val viewModel by viewModels<DeviceListViewModel>()
    private lateinit var searchView: SearchView

    @Inject
    lateinit var deviceListAdapter: DeviceListAdapter

    @Inject
    lateinit var navigator: AppNavigator

    override val showBottomNavigationView = true

    // initial value set to 180 because image needs to be inverted
    private var angle = 180f
    private var filtersExpanded = false

    override fun callOperations() {

    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_userlist)

        searchView = (toolbar.menu.findItem(R.id.actionSearch).actionView as SearchView)
        searchView.maxWidth = Int.MAX_VALUE

        buttonRetry.setOnClickListener {
            makeDevicesRequest()
        }

        recyclerViewDevices.layoutManager = LinearLayoutManager(context)
        recyclerViewDevices.adapter = deviceListAdapter

        layoutFiltering.setOnClickListener {
            filtersExpanded = !filtersExpanded
            layoutFilters.isVisible = filtersExpanded
            angle += 180f
            imageViewSearchArrow.animate().rotation(angle).start()
        }

        setupFilterFields()
        textViewFilterVersionTitle.setOnClickListener {  }
        textViewFilterResolutionTitle.setOnClickListener {  }
    }

    override fun onBindViewModel() {
        makeDevicesRequest()

        observe(viewModel.getUsersOperation) {
            handleGetUsersState(it.state)
            it.doOnSuccess { userInfo ->
                handleInfo(userInfo, viewModel.deviceListLiveData.value ?: emptyList())
            }
        }

        observe(viewModel.filterParameters) {
            refreshFilters(it)
        }
    }

    private fun handleGetUsersState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                viewLoading.isVisible = true
                buttonRetry.isGone = true
                recyclerViewDevices.isVisible = false
            }
            OpState.SUCCESS -> {
                viewLoading.isVisible = false
                buttonRetry.isGone = true
                recyclerViewDevices.isVisible = true
            }
            OpState.FAILURE -> {
                viewLoading.isVisible = false
                buttonRetry.isVisible = true
                recyclerViewDevices.isVisible = false
            }
        }
    }

    private fun setupFilterFields() {
        radioButtonAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                viewModel.setOsType(OS_ALL)
            }
        }
        radioButtonAndroid.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                viewModel.setOsType(OS_ANDROID)
            }
        }
        radioButtonIOS.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                viewModel.setOsType(OS_IOS)
            }
        }

        checkBoxNonPrivate.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setPrivateType(isChecked)
        }

        checkBoxOnlyFree.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setPrivateType(isChecked)
        }

    }

    private fun refreshFilters(filter: DeviceFilter) {
        radioButtonAll.isChecked = filter.os == OS_ALL
        radioButtonAndroid.isChecked = filter.os == OS_ANDROID
        radioButtonIOS.isChecked = filter.os == OS_IOS
    }

    private fun makeDevicesRequest() {
        viewModel.getDevices()
    }

    private fun handleInfo(userList: List<User>, deviceList: List<Device>) {
        deviceListAdapter.applyData(userList, deviceList)
    }

}