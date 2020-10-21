package com.example.mobile_spotter.presentation.userlist

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brandongogetap.stickyheaders.StickyLayoutManager
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.data.navigator.AppNavigator
import com.example.mobile_spotter.data.navigator.Screens
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_user_list.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class UserListFragment : BaseFragment(R.layout.fragment_user_list) {

    private val viewModel by viewModels<UserListViewModel>()
    private lateinit var searchView: SearchView

    @Inject
    lateinit var userListAdapter: UserListAdapter

    @Inject
    lateinit var navigator: AppNavigator

    override val showBottomNavigationView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun callOperations() {

    }

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
    }

    override fun onBindViewModel() {
        makeUsersRequest()

        searchView.queryTextChanges().debounce(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                viewModel.setQuery(it)
            }

        userListAdapter.onUserClickListener = {
            viewModel.selectUser(it)
        }

        observe(viewModel.getUsersOperation) {
            handleGetUsersState(it.state)
            it.doOnSuccess { userInfo ->
                handleGetUsersInfo(userInfo)
            }
        }

        observe(viewModel.queryLiveData) {
            userListAdapter.applyQuery(it.toString())
        }

        observe(viewModel.applyUser) {
            findNavController().navigate(UserListFragmentDirections.actionUserListFragmentToDeviceListFragment())
            // navigator.navigateToRoot(Screens.DEVICES)
        }
    }

    override fun onKeyboardHeightChanged(value: Int) {

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
        if(list.isNotEmpty()) {
            userListAdapter.applyData(list)
        }
    }

    private fun makeUsersRequest() {
        viewModel.getUsers()
    }
}