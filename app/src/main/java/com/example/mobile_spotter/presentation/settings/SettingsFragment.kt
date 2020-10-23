package com.example.mobile_spotter.presentation.settings

import android.os.Bundle
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.User
import com.example.mobile_spotter.ext.fullName
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import com.jakewharton.rxbinding4.appcompat.navigationClicks
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_device_details.buttonRetry
import kotlinx.android.synthetic.main.fragment_device_details.viewLoading
import kotlinx.android.synthetic.main.fragment_settings.*


@AndroidEntryPoint
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private val viewModel by viewModels<SettingsViewModel>()

    override val showBottomNavigationView = true


    override fun callOperations() {

    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        layoutUserName.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToUserListFragment())
        }

        layoutPublicAccount.setOnClickListener {
            checkboxPublicAccount.isChecked = !checkboxPublicAccount.isChecked
        }
    }

    override fun onBindViewModel() {
        toolbar.navigationClicks().subscribe {
            findNavController().popBackStack()
        }

        viewModel.getUsers()

        buttonRetry.setOnClickListener {
            viewModel.getUsers()
        }

        viewModel.getUsersOperation.observe {
            handleGetUsersState(it.state)
            it.doOnSuccess { list ->
                setUserData(list)
            }
        }

        checkboxPublicAccount.isChecked = viewModel.getPublicAccountValue()

        checkboxPublicAccount.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.setPublicAccountValue(isChecked)
        }

        editTextToken.setText(viewModel.getToken())

        buttonApplyToken.setOnClickListener {
            viewModel.setToken(editTextToken.text)
        }

    }

    override fun onKeyboardHeightChanged(value: Int) {

    }

    private fun setUserData(userList: List<User>) {
        val currentUser = userList.filter { it.id == viewModel.getCurrentUserId() }.firstOrNull()
        if(currentUser != null) {
            textViewUserName.text = currentUser.fullName()
        } else {
            textViewUserName.text = getString(R.string.settings_not_defined)
        }
    }

    private fun handleGetUsersState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                viewLoading.isVisible = true
                buttonRetry.isGone = true
                layoutContent.isVisible = false
            }
            OpState.SUCCESS -> {
                viewLoading.isVisible = false
                buttonRetry.isGone = true
                layoutContent.isVisible = true
            }
            OpState.FAILURE -> {
                viewLoading.isVisible = false
                buttonRetry.isVisible = true
                layoutContent.isVisible = false
            }
        }
    }

}