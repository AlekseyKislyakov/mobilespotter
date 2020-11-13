package com.example.mobile_spotter.presentation.devicelist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.data.entities.OS_ALL
import com.example.mobile_spotter.ext.dpToPx
import com.example.mobile_spotter.ext.fullName
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.ext.showSnackbar
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.HolderDecorator
import com.example.mobile_spotter.utils.LongOperation
import com.example.mobile_spotter.utils.OpState
import com.example.mobile_spotter.utils.combineStates
import com.jakewharton.rxbinding4.appcompat.itemClicks
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_device_list.*
import kotlinx.android.synthetic.main.fragment_device_list.buttonRetry
import kotlinx.android.synthetic.main.fragment_device_list.emptyView
import kotlinx.android.synthetic.main.fragment_device_list.toolbar
import kotlinx.android.synthetic.main.fragment_device_list.viewLoading
import kotlinx.android.synthetic.main.view_string_picker.view.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_device_list) {

    private val viewModel by viewModels<DeviceListViewModel>()
    private lateinit var searchView: SearchView

    @Inject
    lateinit var deviceListAdapter: DeviceListAdapter

    @Inject
    lateinit var resolutionListAdapter: CheckBoxListAdapter

    @Inject
    lateinit var versionListAdapter: CheckBoxListAdapter

    private var chooseResolutionDialog: Dialog? = null
    private var chooseVersionDialog: Dialog? = null

    override val showFloatingActionButton = true

    // initial value set to 180 because image needs to be inverted
    private var angle = 180f
    private var filtersExpanded = false

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_devicelist)

        searchView = (toolbar.menu.findItem(R.id.actionSearch).actionView as SearchView)
        searchView.maxWidth = Int.MAX_VALUE

        buttonRetry.setOnClickListener {
            makeDevicesRequest()
        }

        recyclerViewDevices.adapter = deviceListAdapter

        layoutFiltering.setOnClickListener {
            rollFilters()
        }

        shadowView.setOnClickListener {
            rollFilters()
        }

        swipeRefreshLayout.setOnRefreshListener {
            makeDevicesRequest()
        }

        toolbar.itemClicks().filter { it.itemId == R.id.actionProfile }.subscribe {
            findNavController().navigate(DeviceListFragmentDirections.actionDeviceListFragmentToSettingsFragment())
        }

        setupFilterFields()
        buttonChooseUser.setOnClickListener {
            findNavController().navigate(DeviceListFragmentDirections.actionDeviceListFragmentToUserListFragment())
        }

        buttonResetUser.setOnClickListener {
            viewModel.userId = -1
            refreshActionButtons()
            showSnackbar(getString(R.string.device_list_user_reset))
        }

        refreshActionButtons()

        toolbar.setNavigationOnClickListener {
            deviceListAdapter.clearSelection()
        }
    }

    override fun onCodeRecognized(code: String) {
        val entity = viewModel.handleCode(code)
        if (entity != null && entity is User) {
            showSnackbar(getString(R.string.user_list_choose_owner, entity.fullName()))
        } else if (entity is Device) {
            findNavController().navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceDetailsFragment(entity.id.toString()))
        } else {
            showSnackbar(getString(R.string.common_card))
        }
        refreshActionButtons()
    }

    override fun onBindViewModel() {
        makeDevicesRequest()

        recyclerViewDevices.addItemDecoration(HolderDecorator(8.dpToPx()))

        deviceListAdapter.onClickListener = {
            viewModel.setDevice(it)
            findNavController().navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceDetailsFragment(it.id.toString()))
        }

        deviceListAdapter.onItemSelected = {
            showSelectionInToolbar()
        }

        deviceListAdapter.onEmptyListAction = {
            emptyView.isVisible = it
        }

        searchView.queryTextChanges().debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                    viewModel.setQuery(it)
                }
    }

    override fun observeOperations() {
        observe(viewModel.getUsersOperation) {
            handleGetDevicesState(combineStates(listOf(it.state, viewModel.getDevicesOperation.value?.state)))
            it.doOnSuccess { userInfo ->
                handleInfo(userInfo, viewModel.deviceListLiveData.value ?: emptyList())
            }
        }

        observe(viewModel.getDevicesOperation) {
            handleGetDevicesState(combineStates(listOf(it.state, viewModel.getUsersOperation.value?.state)))
        }

        observe(viewModel.queryLiveData) { query ->
            viewModel.filterParameters.let { filter ->
                deviceListAdapter.applyFilter(filter, query)
            }
        }

        observe(viewModel.refreshFilters) {
            refreshFilters(viewModel.filterParameters)
        }
    }

    override fun onKeyboardHeightChanged(value: Int) {
        if (value != -1) {
            hideBottomNavigation()
        } else {
            GlobalScope.launch(context = Dispatchers.Main) {
                delay(50)
                showBottomNavigation()
            }
        }
    }

    private fun showSelectionInToolbar() {
        if(deviceListAdapter.selectedCount > 0) {
            toolbar.setNavigationIcon(R.drawable.ic_clear_black)
            toolbar.title = deviceListAdapter.selectedCount.toString()
        } else {
            toolbar.navigationIcon = null
            toolbar.setTitle(R.string.device_list_choose_device)
        }
        refreshActionButtons()

    }

    private fun handleGetDevicesState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                emptyView.isGone = true
                viewLoading.isVisible = true
                buttonRetry.isGone = true
                recyclerViewDevices.isVisible = false
            }
            OpState.SUCCESS -> {
                viewLoading.isVisible = false
                buttonRetry.isGone = true
                recyclerViewDevices.isVisible = true
                swipeRefreshLayout.isRefreshing = false
            }
            OpState.FAILURE -> {
                emptyView.isGone = true
                viewLoading.isVisible = false
                buttonRetry.isVisible = true
                recyclerViewDevices.isVisible = false
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupFilterFields() {
        radioButtonAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.setOsType(OS_ALL)
            }
        }
        radioButtonAndroid.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.setOsType(OS_ANDROID)
            }
        }
        radioButtonIOS.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.setOsType(OS_IOS)
            }
        }

        textViewFilterResolutionTitle.setOnClickListener {
            createChooseResolutionDialog()?.show()
        }

        textViewFilterVersionTitle.setOnClickListener {
            createChooseOSVersionDialog()?.show()
        }

        checkBoxNonPrivate.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setPrivateType(isChecked)
        }

        checkBoxOnlyFree.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setFreeType(isChecked)
        }

    }

    private fun refreshActionButtons() {
        layoutResetUser.isVisible = viewModel.userId != -1 && deviceListAdapter.selectedCount == 0
        layoutSelectUser.isVisible = viewModel.userId == -1
        layoutTakeOrReturn.isVisible = deviceListAdapter.selectedCount > 0 && viewModel.userId != -1
    }

    private fun rollFilters() {
        filtersExpanded = !filtersExpanded
        layoutFilters.isVisible = filtersExpanded
        shadowView.isVisible = filtersExpanded
        angle += 180f
        imageViewSearchArrow.animate().rotation(angle).start()
    }

    private fun refreshFilters(filter: DeviceFilter) {
        radioButtonAll.isChecked = filter.os == OS_ALL
        radioButtonAndroid.isChecked = filter.os == OS_ANDROID
        radioButtonIOS.isChecked = filter.os == OS_IOS

        resolutionListAdapter.replaceItems(
                filter.resolutionSet.toList(),
                filter.selectedResolutionSet.toList(),
                filter.os
        )

        versionListAdapter.replaceItems(
                filter.versionSet.toList(),
                filter.selectedVersionSet.toList(),
                filter.os
        )

        deviceListAdapter.applyFilter(filter, viewModel.queryLiveData.value ?: "")
    }

    private fun makeDevicesRequest() {
        viewModel.getDevices()
    }

    private fun handleInfo(userList: List<User>, deviceList: List<Device>) {
        deviceListAdapter.applyData(userList, deviceList)
    }

    private fun createChooseResolutionDialog(): Dialog? {
        activity?.let { context ->
            if (chooseResolutionDialog == null) {
                val chooseConverterView = LayoutInflater.from(context)
                        .inflate(R.layout.view_string_picker, null)

                with(chooseConverterView) {
                    resolutionListAdapter.selectedSet =
                            viewModel.filterParameters.selectedResolutionSet
                    recyclerViewChooseString.apply {
                        layoutManager = LinearLayoutManager(context)
                        isNestedScrollingEnabled = false
                        adapter = resolutionListAdapter
                    }

                    buttonApply.setOnClickListener {
                        viewModel.setResolutionList(resolutionListAdapter.selectedSet)
                        chooseResolutionDialog?.dismiss()
                    }

                    chooseResolutionDialog = Dialog(context, R.style.DialogTheme).apply {
                        setCancelable(true)
                        setCanceledOnTouchOutside(true)
                        setContentView(chooseConverterView)
                    }
                }
            }
        }

        return chooseResolutionDialog
    }

    private fun createChooseOSVersionDialog(): Dialog? {
        activity?.let { context ->
            if (chooseVersionDialog == null) {
                val chooseVersionView = LayoutInflater.from(context)
                        .inflate(R.layout.view_string_picker, null)

                with(chooseVersionView) {
                    versionListAdapter.selectedSet =
                            viewModel.filterParameters.selectedVersionSet
                    recyclerViewChooseString.apply {
                        layoutManager = LinearLayoutManager(context)
                        isNestedScrollingEnabled = false
                        adapter = versionListAdapter
                    }

                    buttonApply.setOnClickListener {
                        viewModel.setOsList(versionListAdapter.selectedSet)
                        chooseVersionDialog?.dismiss()
                    }

                    chooseVersionDialog = Dialog(context, R.style.DialogTheme).apply {
                        setCancelable(true)
                        setCanceledOnTouchOutside(true)
                        setContentView(chooseVersionView)
                    }
                }
            }
        }

        return chooseVersionDialog
    }

}