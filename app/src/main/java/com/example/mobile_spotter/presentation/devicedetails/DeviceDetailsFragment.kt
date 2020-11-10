package com.example.mobile_spotter.presentation.devicedetails

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.ANDROID
import com.example.mobile_spotter.data.entities.Device
import com.example.mobile_spotter.data.entities.OS_ANDROID
import com.example.mobile_spotter.data.entities.User
import com.example.mobile_spotter.ext.fullName
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.ext.showActionSnackbar
import com.example.mobile_spotter.ext.showSnackbar
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import com.jakewharton.rxbinding4.appcompat.navigationClicks
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_device_details.*
import kotlinx.android.synthetic.main.fragment_device_details.buttonRetry
import kotlinx.android.synthetic.main.fragment_device_details.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_device_details.viewLoading

@AndroidEntryPoint
class DeviceDetailsFragment : BaseFragment(R.layout.fragment_device_details) {

    private val viewModel by viewModels<DeviceDetailsViewModel>()

    private var refreshFilters = false
    private var deviceId: String? = null

    val args: DeviceDetailsFragmentArgs by navArgs()

    private var deviceInfo: Device? = null

    override val showFloatingActionButton = true

    override fun observeOperations() {
        observe(viewModel.getUsersOperation) {
            handleGetDevicesState(it.state)
            it.doOnSuccess { userInfo ->
                handleInfo(userInfo, viewModel.deviceListLiveData.value ?: emptyList())
            }
        }

        observe(viewModel.getDevicesOperation) {
            handleGetDevicesState(it.state)
        }

        observe(viewModel.takeDeviceLiveData) {
            handleTakeDeviceState(it.state)
            it.doOnSuccess {
                AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.device_details_taken))
                        .setPositiveButton(getString(R.string.common_ok), null)
                        .setOnDismissListener {
                            makeDevicesRequest()
                        }
                        .show()
            }
        }
        observe(viewModel.returnDeviceLiveData) {
            handleReturnDeviceState(it.state)
            it.doOnSuccess {
                AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.device_details_returned))
                        .setPositiveButton(getString(R.string.common_ok), null)
                        .setOnDismissListener {
                            makeDevicesRequest()
                        }
                        .show()
            }
        }
    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        buttonRetry.setOnClickListener {
            makeDevicesRequest()
        }
    }

    override fun onBindViewModel() {
        if (viewModel.isPublic == true) {
            layoutGeneralActions.isVisible = true
            layoutPrivateActions.isGone = true
        } else {
            layoutGeneralActions.isGone = true
            layoutPrivateActions.isVisible = true
        }
        makeDevicesRequest()

        buttonTakeDevice.setOnClickListener {
            viewModel.takeDevice()
        }
        buttonReturnDevice.setOnClickListener {
            viewModel.returnDevice()
        }
        buttonTakeDeviceGeneral.setOnClickListener {
            viewModel.takeDevice()
        }
        buttonReturnDeviceGeneral.setOnClickListener {
            viewModel.returnDevice()
        }

        swipeRefreshLayout.setOnRefreshListener {
            makeDevicesRequest()
        }

        toolbar.navigationClicks().subscribe {
            findNavController().popBackStack()
        }

        deviceId = args.deviceId
    }

    override fun onCodeRecognized(code: String) {
        val entity = viewModel.handleCode(code)
        if (entity != null && entity is User) {
            showSnackbar(getString(R.string.user_list_choose_owner, entity.fullName()))
        } else if (entity is Device) {
            deviceId = entity.id.toString()
            makeDevicesRequest()
        } else {
            showSnackbar("Неизвестная карта")
        }
    }

    override fun onKeyboardHeightChanged(value: Int) {
//        if (value != -1) {
//            hideBottomNavigation()
//        } else {
//            GlobalScope.launch(context = Dispatchers.Main) {
//                delay(50)
//                showBottomNavigation()
//            }
//        }
    }

    private fun handleGetDevicesState(state: OpState) {
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
                swipeRefreshLayout.isRefreshing = false
            }
            OpState.FAILURE -> {
                viewLoading.isVisible = false
                buttonRetry.isVisible = true
                layoutContent.isVisible = false
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun handleTakeDeviceState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                progressBarTake.isVisible = true
                buttonTakeDevice.isClickable = false
                buttonTakeDevice.text = ""
            }
            OpState.SUCCESS -> {
                progressBarTake.isGone = true
                buttonTakeDevice.isClickable = true
                buttonTakeDevice.text = getString(R.string.device_details_take)
            }
            OpState.FAILURE -> {
                progressBarTake.isGone = true
                buttonTakeDevice.isClickable = true
                buttonTakeDevice.text = getString(R.string.device_details_take)
            }
        }
    }

    private fun handleReturnDeviceState(state: OpState) {
        when (state) {
            OpState.LOADING -> {
                progressBarReturn.isVisible = true
                buttonReturnDevice.isClickable = false
                buttonReturnDevice.text = ""
            }
            OpState.SUCCESS -> {
                progressBarReturn.isGone = true
                buttonReturnDevice.isClickable = true
                buttonReturnDevice.text = getString(R.string.device_details_return)
            }
            OpState.FAILURE -> {
                progressBarReturn.isGone = true
                buttonReturnDevice.isClickable = true
                buttonReturnDevice.text = getString(R.string.device_details_return)
            }
        }
    }

    private fun makeDevicesRequest() {
        viewModel.getDevices()
    }

    private fun handleInfo(userList: List<User>, deviceList: List<Device>) {
        deviceInfo = deviceList.firstOrNull { deviceId == it.id.toString() }
        deviceInfo?.let { deviceInfo ->
            //required fields
            if (deviceInfo.osType.toLowerCase() == OS_ANDROID) {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_android_robot)
            } else {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_apple_logo_grey)
            }

            textViewDeviceName.text = deviceInfo.name
            textViewDeviceOS.text = "${deviceInfo.osType} ${deviceInfo.osVersion}"
            textViewDeviceResolution.text = deviceInfo.resolution
            textViewDeviceType.text = deviceInfo.type

            // optional fields
            if (deviceInfo.nickname.isNotEmpty()) {
                textViewDeviceNickname.text = deviceInfo.nickname
                textViewDeviceNickname.isVisible = true
                textViewDeviceNicknameTitle.isVisible = true
            } else {
                textViewDeviceNickname.isGone = true
                textViewDeviceNicknameTitle.isGone = true
            }

            if (deviceInfo.shell.isNotEmpty()) {
                textViewDeviceShell.text = deviceInfo.shell
                textViewDeviceShell.isVisible = true
                textViewDeviceShellTitle.isVisible = true
            } else {
                textViewDeviceShell.isGone = true
                textViewDeviceShellTitle.isGone = true
            }

            if (deviceInfo.comment.isNotEmpty()) {
                textViewDeviceComment.text = deviceInfo.comment
                textViewDeviceComment.isVisible = true
                textViewDeviceCommentTitle.isVisible = true
            } else {
                textViewDeviceComment.isGone = true
                textViewDeviceCommentTitle.isGone = true
            }

            val currentUser = userList.firstOrNull { user -> user.id == viewModel.originId }

            currentUser?.let {
                textViewUserName.text = it.fullName()
                buttonTakeDeviceGeneral.text = getString(R.string.device_details_take_as, it.fullName())
                buttonReturnDeviceGeneral.text = getString(R.string.device_details_return_as, it.fullName())
            } ?: run {
                textViewUserName.text = getString(R.string.common_not_defined)
                buttonTakeDeviceGeneral.text = getString(R.string.device_details_take)
                buttonReturnDeviceGeneral.text = getString(R.string.device_details_return)
            }

            layoutUserName.setOnClickListener {
                findNavController().navigate(DeviceDetailsFragmentDirections.actionDeviceListFragmentToUserListFragment())
            }

            // check owner and create listener to open telegram
            if (deviceInfo.ownerId == 0 || deviceInfo.ownerId == -1) {
                textViewDeviceStatus.text = getString(R.string.device_details_free)
                buttonTakeDevice.isEnabled = true
                buttonTakeDeviceGeneral.isEnabled = true
                textViewDeviceStatus.setTextColor(
                        ResourcesCompat.getColor(
                                resources,
                                R.color.soft_green,
                                null
                        )
                )
                imageViewTelegramIcon.isVisible = false
                textViewDeviceStatus.isClickable = false
                buttonReturnDevice.isEnabled = false
                buttonReturnDeviceGeneral.isEnabled = false
            } else {
                val owner = userList.firstOrNull { deviceInfo.ownerId == it.id }
                if (owner?.id == viewModel.originId) {
                    textViewDeviceStatus.text = getString(R.string.device_details_your_rent)
                    imageViewTelegramIcon.isVisible = false
                    textViewDeviceStatus.isClickable = false
                    buttonReturnDevice.isEnabled = true
                    buttonReturnDeviceGeneral.isEnabled = true
                    buttonTakeDevice.isEnabled = false
                    buttonTakeDeviceGeneral.isEnabled = false
                } else {
                    owner?.let { owner ->
                        textViewDeviceStatus.text = getString(R.string.device_list_busy, owner.fullName())
                        imageViewTelegramIcon.isVisible = true
                        textViewDeviceStatus.isClickable = true
                        buttonReturnDevice.isEnabled = false
                        buttonReturnDeviceGeneral.isEnabled = false

                        textViewDeviceStatus.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.t.me/${owner.telegram}"))
                            try {
                                requireContext().startActivity(intent)
                            } catch (ex: ActivityNotFoundException) {
                                intent.setPackage(null)
                                requireContext().startActivity(intent)
                            }
                        }
                    }
                }

                textViewDeviceStatus.setTextColor(
                        ResourcesCompat.getColor(
                                resources,
                                R.color.soft_red,
                                null
                        )
                )
            }

            if (currentUser == null) {
                buttonTakeDevice.isEnabled = false
                buttonReturnDevice.isEnabled = false
                buttonTakeDeviceGeneral.isEnabled = false
                buttonReturnDeviceGeneral.isEnabled = false
                showActionSnackbar(getString(R.string.device_details_user_not_defined)) {
                    findNavController().navigate(DeviceDetailsFragmentDirections.actionDeviceListFragmentToUserListFragment())
                }
            }
        }
    }
}