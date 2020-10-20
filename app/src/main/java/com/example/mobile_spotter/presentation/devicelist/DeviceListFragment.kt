package com.example.mobile_spotter.presentation.devicelist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
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
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_devicelist.*
import kotlinx.android.synthetic.main.view_string_picker.view.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DeviceListFragment : BaseFragment(R.layout.fragment_devicelist) {

    private val viewModel by viewModels<DeviceListViewModel>()
    private lateinit var searchView: SearchView

    @Inject
    lateinit var deviceListAdapter: DeviceListAdapter

    @Inject
    lateinit var resolutionListAdapter: CheckBoxListAdapter

    @Inject
    lateinit var versionListAdapter: CheckBoxListAdapter

    @Inject
    lateinit var navigator: AppNavigator

    private var chooseResolutionDialog: Dialog? = null
    private var chooseVersionDialog: Dialog? = null

    private var refreshFilters = false

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
            rollFilters()
        }

        shadowView.setOnClickListener {
            rollFilters()
        }

        setupFilterFields()
    }

    override fun onBindViewModel() {
        makeDevicesRequest()

        searchView.queryTextChanges().debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                    viewModel.setQuery(it)
                }

        observe(viewModel.getUsersOperation) {
            handleGetDevicesState(it.state)
            it.doOnSuccess { userInfo ->
                handleInfo(userInfo, viewModel.deviceListLiveData.value ?: emptyList())
            }
        }

        observe(viewModel.getDevicesOperation) {
            handleGetDevicesState(it.state)
        }

        observe(viewModel.filterParameters) {
            refreshFilters(it)
        }

        observe(viewModel.queryLiveData) { query ->
            viewModel.filterParameters.value?.let { filter ->
                deviceListAdapter.applyFilter(filter, query)
            }

        }

        observe(viewModel.refreshFilters) {
            refreshFilters = true
        }
    }

    private fun handleGetDevicesState(state: OpState) {
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

        refreshFilters = false
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
                            viewModel.filterParameters.value?.selectedResolutionSet ?: mutableSetOf()
                    recyclerViewChooseString.apply {
                        layoutManager = LinearLayoutManager(context)
                        isNestedScrollingEnabled = false
                        adapter = resolutionListAdapter
                    }

                    buttonApply.setOnClickListener {
                        viewModel.setResolutionList(resolutionListAdapter.selectedSet)
                        chooseResolutionDialog?.dismiss()
                    }

                    buttonDiscard.setOnClickListener {
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
                            viewModel.filterParameters.value?.selectedVersionSet ?: mutableSetOf()
                    recyclerViewChooseString.apply {
                        layoutManager = LinearLayoutManager(context)
                        isNestedScrollingEnabled = false
                        adapter = versionListAdapter
                    }

                    buttonApply.setOnClickListener {
                        viewModel.setOsList(versionListAdapter.selectedSet)
                        chooseVersionDialog?.dismiss()
                    }

                    buttonDiscard.setOnClickListener {
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