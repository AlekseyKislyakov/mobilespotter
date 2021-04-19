package com.example.mobile_spotter.presentation.userlist

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.brandongogetap.stickyheaders.StickyLayoutManager
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.ext.*
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.presentation.userlist.UserListAdapter.Companion.HEADER_VIEW_TYPE
import com.example.mobile_spotter.presentation.userlist.UserListAdapter.Companion.USER_VIEW_TYPE
import com.example.mobile_spotter.utils.OpState
import com.example.mobile_spotter.utils.StickyHolderDecorator
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

    val SINGLE_COLUMN = 1

    private val viewModel by viewModels<UserListViewModel>()
    private lateinit var searchView: SearchView

    @Inject
    lateinit var userListAdapter: UserListAdapter

    @Inject
    lateinit var sectionListAdapter: SectionListAdapter

    override val showFloatingActionButton = true

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_userlist)

        searchView = (toolbar.menu.findItem(R.id.actionSearch).actionView as SearchView)
        searchView.maxWidth = Int.MAX_VALUE

        buttonRetry.setOnClickListener {
            makeUsersRequest()
        }

        val spanCount = resources.getInteger(R.integer.device_details_column_count_tile)

        if(spanCount == SINGLE_COLUMN) {
            recyclerViewUsers.layoutManager = StickyLayoutManager(context, userListAdapter)
            coordinatorLayout.setBackgroundColor(getColor((R.color.white)))
        } else {
            val layoutManager = GridLayoutManager(context, spanCount)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (userListAdapter.getItemViewType(position)) {
                        HEADER_VIEW_TYPE -> spanCount
                        USER_VIEW_TYPE -> 1
                        else -> -1
                    }
                }
            }
            recyclerViewUsers.layoutManager = layoutManager
            recyclerViewUsers.addItemDecoration(StickyHolderDecorator(8.dpToPx()))
        }

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
            showSnackbar(getString(R.string.common_unknown_card))
        }
    }

    override fun logoutTimerEvent() {
        findNavController().navigate(UserListFragmentDirections.actionUserListToDeviceListFragment("0"))
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
                showSnackbar(getString(R.string.common_network_error))
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