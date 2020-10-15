package com.example.mobile_spotter.presentation.userlist

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.UserList
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_users.*

@AndroidEntryPoint
class UserListFragment : BaseFragment(R.layout.fragment_users) {

    private val viewModel by viewModels<UserListViewModel>()
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun callOperations() {

    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.menu_userlist)

        searchView = (toolbar.menu.findItem(R.id.actionSearch).actionView as SearchView)
        searchView.maxWidth = Int.MAX_VALUE
    }

    override fun onBindViewModel() {
        viewModel.getUsers()

        observe(viewModel.liveData) {
            handleGetUsersState(it.state)
            it.doOnSuccess {
                handleGetUsersInfo(it)
            }
        }
    }

    private fun handleGetUsersState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                viewLoading.isVisible = true
            }
            OpState.SUCCESS -> {
                viewLoading.isVisible = false
            }
            OpState.FAILURE -> {

            }
        }
    }

    private fun handleGetUsersInfo(list: UserList) {
        toolbar.title = "Success"
    }
}