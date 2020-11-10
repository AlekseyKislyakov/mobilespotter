package com.example.mobile_spotter.presentation.userlist

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brandongogetap.stickyheaders.StickyLayoutManager
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.ext.fullName
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.ext.showSnackbar
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.presentation.devicelist.DeviceListFragmentDirections
import com.example.mobile_spotter.utils.OpState
import com.jakewharton.rxbinding4.appcompat.navigationClicks
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.fragment_user_list.buttonRetry
import kotlinx.android.synthetic.main.fragment_user_list.toolbar
import kotlinx.android.synthetic.main.fragment_user_list.viewLoading
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class UserListFragment : BaseFragment(R.layout.fragment_user_list) {

    private val viewModel by viewModels<UserListViewModel>()
    private lateinit var searchView: SearchView

    @Inject
    lateinit var userListAdapter: UserListAdapter

    @Inject
    lateinit var sectionListAdapter: SectionListAdapter

    override val showFloatingActionButton = false

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_userlist)

        searchView = (toolbar.menu.findItem(R.id.actionSearch).actionView as SearchView)
        searchView.maxWidth = Int.MAX_VALUE

        buttonRetry.setOnClickListener {
            makeUsersRequest()
        }

        val stickyLayoutManager = StickyLayoutManager(context, userListAdapter)
        recyclerViewUsers.layoutManager = stickyLayoutManager
        recyclerViewUsers.adapter = userListAdapter

        recyclerViewSectionFilters.adapter = sectionListAdapter
    }

    override fun onBindViewModel() {
        makeUsersRequest()

        toolbar.navigationClicks().subscribe {
            findNavController().popBackStack()
        }

        searchView.queryTextChanges().debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe {
                    viewModel.setQuery(it)
                }

        userListAdapter.onUserClickListener = {
            viewModel.selectUser(it)
            showSnackbar(getString(R.string.user_list_choose_owner, it.fullName()))
        }

        userListAdapter.onEmptyListAction = {
            emptyView.isVisible = it
        }

        sectionListAdapter.onSectionListChangeListener = {
            userListAdapter.applySectionList(it)
        }
    }

    override fun observeOperations() {
        observe(viewModel.getUsersOperation) {
            handleGetUsersState(it.state)
            it.doOnSuccess { userInfo ->
                handleGetUsersInfo(userInfo)
            }
        }

        observe(viewModel.queryLiveData) {
            userListAdapter.applyFilters(it.toString())
        }

        observe(viewModel.applyUser) {
            findNavController().popBackStack()
        }
    }

    override fun onKeyboardHeightChanged(value: Int) {

    }

    override fun onCodeRecognized(code: String) {
        val entity = viewModel.handleCode(code)
        if (entity != null && entity is User) {
            showSnackbar(getString(R.string.user_list_choose_owner, entity.fullName()))
        } else {
            showSnackbar("Неизвестная карта")
        }
    }

    private fun handleGetUsersState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                viewLoading.isVisible = true
                buttonRetry.isGone = true
                recyclerViewUsers.isVisible = false
            }
            OpState.SUCCESS -> {
                viewLoading.isVisible = false
                buttonRetry.isGone = true
                recyclerViewUsers.isVisible = true
            }
            OpState.FAILURE -> {
                viewLoading.isVisible = false
                buttonRetry.isVisible = true
                recyclerViewUsers.isVisible = false
            }
        }
    }

    private fun handleGetUsersInfo(list: UserList) {
        if (list.isNotEmpty()) {
            val sectionList = list.map {
                Section(it.department.toString(), getString(it.department.recognize()), false)
            }.toMutableList()
            sectionList.add(0, Section(ALL, context?.getString(R.string.common_all) ?: "", true))

            userListAdapter.applyData(list)
            sectionListAdapter.setItems(sectionList)
            recyclerViewSectionFilters.isVisible = true

        }
    }

    private fun makeUsersRequest() {
        viewModel.getUsers()
    }
}