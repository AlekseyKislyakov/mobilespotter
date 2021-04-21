package com.example.mobile_spotter.presentation.devicelist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.data.entities.OS_ALL
import com.example.mobile_spotter.ext.*
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.HolderDecorator
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_device_list) {
    companion object {
        const val DEVICE_LIST_REQUEST = "device_list_request"
        const val EXTRA_SELECTED_LIST = "extra_selected_list"
    }

    private val viewModel by viewModels<DeviceListViewModel>()
    private lateinit var searchView: SearchView

    @Inject lateinit var deviceListAdapter: DeviceListAdapter

    @Inject lateinit var resolutionListAdapter: CheckBoxListAdapter

    @Inject lateinit var versionListAdapter: CheckBoxListAdapter

    private var chooseResolutionDialog: Dialog? = null
    private var chooseVersionDialog: Dialog? = null

    override val showFloatingActionButton = true

    // initial value set to 180 because image needs to be inverted
    private var angle = 180f
    private var filtersExpanded = false

    private var mode = DeviceListAdapter.DeviceListMode.ROW

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_devicelist)

        searchView = (toolbar.menu.findItem(R.id.actionSearch).actionView as SearchView)
        searchView.maxWidth = Int.MAX_VALUE
        if (searchView.isFocused) {
            showSoftKeyboard(searchView)
        }

        buttonRetry.setOnClickListener {
            makeDevicesRequest()
        }
        val layoutManager = if (mode == DeviceListAdapter.DeviceListMode.TILE) {
            recyclerViewDevices.addItemDecoration(HolderDecorator(8.dpToPx()))
            GridLayoutManager(requireContext(), resources.getInteger(R.integer.device_details_column_count_tile))
        } else {
            recyclerViewDevices.addItemDecoration(HolderDecorator(4.dpToPx()))
            GridLayoutManager(requireContext(), resources.getInteger(R.integer.device_details_column_count_row))
        }
        recyclerViewDevices.layoutManager = layoutManager
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
            findNavController().navigate(
                DeviceListFragmentDirections.actionDeviceListFragmentToUserListFragment(
                    deviceListAdapter.getSelectedDevices().map { it.device.id }.toIntArray())
            )
        }

        refreshActionButtons()

        buttonResetUser.setOnClickListener {
            viewModel.userId = 0
            deviceListAdapter.currentUserId = -1
            refreshActionButtons()
            showSnackbar(getString(R.string.device_list_user_reset))
        }

        toolbar.setNavigationOnClickListener {
            deviceListAdapter.clearSelection()
            viewModel.clearSelection()
        }

        deviceListAdapter.onClickListener = {
            viewModel.setDevice(it)
            findNavController().navigate(DeviceListFragmentDirections.actionDeviceListFragmentToDeviceDetailsFragment(it.id.toString()))
        }

        deviceListAdapter.onItemSelected = {
            showSelectionInToolbar()
            it?.let {
                viewModel.handleSelection(it)
            }
        }

        deviceListAdapter.onItemUnselected = { device ->
            showSelectionInToolbar()
            device?.let {
                viewModel.handleUnselection(device)
            }
        }

        deviceListAdapter.onEmptyListAction = {
            emptyView.isVisible = it
        }

        switchViewMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                deviceListAdapter.mode = DeviceListAdapter.DeviceListMode.TILE
                mode = DeviceListAdapter.DeviceListMode.TILE
            } else {
                deviceListAdapter.mode = DeviceListAdapter.DeviceListMode.ROW
                mode = DeviceListAdapter.DeviceListMode.ROW
            }
            viewModel.viewMode = isChecked
            refreshRecyclerViewParams()
            deviceListAdapter.notifyDataSetChanged()
        }

        buttonTakeOrReturn.setOnClickListener {
            viewModel.takeOrReturnDevices(deviceListAdapter.getSelectedDevices())
        }

        setFragmentResultListener(DEVICE_LIST_REQUEST) { _, bundle ->
            viewModel.setSelectedList(bundle.getIntArray(EXTRA_SELECTED_LIST))
        }
    }

    override fun onCodeRecognized(code: String) {
        val entity = viewModel.handleCode(code)
        if (entity != null && entity is User) {
            showRFIDMessage(getString(R.string.user_list_choose_owner, entity.fullName()))
            deviceListAdapter.currentUserId = entity.id
        } else if (entity is Device) {
            deviceListAdapter.selectByCode(entity)
            showRFIDMessage(entity.detailedName(requireContext()))
        } else {
            showSnackbar(getString(R.string.common_card))
        }
        refreshActionButtons()
    }

    override fun setRFIDButtonElevation(value: Int) {
        super.setRFIDButtonElevation(80)
    }

    override fun onBindViewModel() {
        switchOrdering.isChecked = viewModel.mainSorting ?: false
        handleMainOrdering(switchOrdering.isChecked)
        switchIncreasingDecreasing.isChecked = viewModel.alphabeticalSorting ?: false
        handleAlphabeticalOrdering(switchIncreasingDecreasing.isChecked)
        switchViewMode.isChecked = viewModel.viewMode ?: false

        makeDevicesRequest()

        searchView.queryTextChanges().debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                viewModel.setQuery(it)
            }
    }

    override fun observeOperations() {
        observe(viewModel.getUsersOperation) { operation ->
            handleGetDevicesState(combineStates(listOf(operation.state, viewModel.getDevicesOperation.value?.state)))
            operation.doOnSuccess { userInfo ->
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
            viewModel.clearSelection()
        }

        observe(viewModel.selectionValue) { selection ->
            if (selection.toBeTaken > 0 && selection.toBeReturned > 0) {
                buttonTakeOrReturn.text = getString(
                    R.string.device_list_take_and_return,
                    selection.toBeTaken.toString(),
                    selection.toBeReturned.toString()
                )
            } else if (selection.toBeTaken > 0) {
                buttonTakeOrReturn.text = getString(R.string.device_list_take_n, selection.toBeTaken.toString())
            } else if (selection.toBeReturned > 0) {
                buttonTakeOrReturn.text = getString(R.string.device_list_return_n, selection.toBeReturned.toString())
            }
        }

        observe(viewModel.moveDevicesOperation) {
            handleMoveDeviceState(it.state)
        }
    }

    override fun logoutTimerEvent() {
        viewModel.userId = 0
        makeDevicesRequest()
        refreshActionButtons()
    }

    private fun showSelectionInToolbar() {
        if (deviceListAdapter.selectedCount > 0) {
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
                swipeRefreshLayout.isRefreshing = false
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
                showSnackbar(getString(R.string.common_network_error))
            }
        }
    }

    private fun handleMoveDeviceState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                buttonTakeOrReturn.isEnabled = false
            }
            OpState.SUCCESS -> {
                buttonTakeOrReturn.isEnabled = true
                showSnackbar(getString(R.string.device_list_success))
                deviceListAdapter.clearSelection()
                viewModel.clearSelection()
                viewModel.getDevices()
            }
            OpState.FAILURE -> {
                buttonTakeOrReturn.isEnabled = true
                showSnackbar(getString(R.string.common_network_error))
            }
        }
    }

    private fun setupFilterFields() {
        switchOrdering.setOnCheckedChangeListener { _, isChecked ->
            handleMainOrdering(isChecked)
            viewModel.mainSorting = isChecked
        }
        switchIncreasingDecreasing.setOnCheckedChangeListener { _, isChecked ->
            handleAlphabeticalOrdering(isChecked)
            viewModel.alphabeticalSorting = isChecked
        }
        radioButtonAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setOsType(OS_ALL)
            }
        }
        radioButtonAndroid.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setOsType(OS_ANDROID)
            }
        }
        radioButtonIOS.setOnCheckedChangeListener { _, isChecked ->
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

        checkBoxNonPrivate.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPrivateType(isChecked)
        }

        checkBoxOnlyFree.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFreeType(isChecked)
        }

        checkBoxOnlyMine.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setOnlyMine(isChecked)
        }
    }

    private fun refreshActionButtons() {
        layoutResetUser.isVisible = viewModel.userId != 0 && deviceListAdapter.selectedCount == 0
        layoutSelectUser.isVisible = viewModel.userId == 0
        layoutTakeOrReturn.isVisible = deviceListAdapter.selectedCount > 0 && viewModel.userId != 0
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
        viewModel.selectionArray.value?.let { deviceListAdapter.selectById(it) }
        viewModel.setSelectedList(null)
        deviceListAdapter.currentUserId = viewModel.userId ?: 0
    }

    private fun createChooseResolutionDialog(): Dialog? {
        activity?.let { context ->
            if (chooseResolutionDialog == null) {
                val chooseResolutionView = LayoutInflater.from(context)
                    .inflate(R.layout.view_string_picker, null)

                with(chooseResolutionView) {
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
                    buttonCheckAll.setOnClickListener {
                        resolutionListAdapter.selectOrRemoveSelection()
                    }
                    chooseResolutionView.post {
                        chooseResolutionView.scrollViewStringPicker.scrollTo(0, 0)
                    }
                    chooseResolutionDialog = Dialog(context, R.style.DialogTheme).apply {
                        setCancelable(true)
                        setCanceledOnTouchOutside(true)
                        setContentView(chooseResolutionView)
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
                    buttonCheckAll.setOnClickListener {
                        versionListAdapter.selectOrRemoveSelection()
                    }
                    chooseVersionView.post {
                        chooseVersionView.scrollViewStringPicker.scrollTo(0, 0)
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

    private fun handleMainOrdering(isChecked: Boolean) {
        if (isChecked) {
            viewModel.setMainOrdering(ORDERING_BY_VERSION)
        } else {
            viewModel.setMainOrdering(ORDERING_AS_IS)
        }
    }

    private fun handleAlphabeticalOrdering(isChecked: Boolean) {
        if (isChecked) {
            viewModel.setAlphabeticalOrdering(ORDERING_DECREASING)
        } else {
            viewModel.setAlphabeticalOrdering(ORDERING_INCREASING)
        }
    }

    private fun refreshRecyclerViewParams() {
        val layoutManager = if (mode == DeviceListAdapter.DeviceListMode.TILE) {
            GridLayoutManager(requireContext(), resources.getInteger(R.integer.device_details_column_count_tile))
        } else {
            GridLayoutManager(requireContext(), resources.getInteger(R.integer.device_details_column_count_row))
        }
        recyclerViewDevices.layoutManager = layoutManager
    }
}