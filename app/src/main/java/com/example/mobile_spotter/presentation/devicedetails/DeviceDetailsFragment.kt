package com.example.mobile_spotter.presentation.devicedetails

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.Device
import com.example.mobile_spotter.data.entities.User
import com.example.mobile_spotter.ext.observe
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.OpState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_device_details.*
import kotlinx.android.synthetic.main.fragment_device_details.buttonRetry
import kotlinx.android.synthetic.main.fragment_device_details.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_device_details.viewLoading
import kotlinx.android.synthetic.main.fragment_device_list.*


@AndroidEntryPoint
class DeviceDetailsFragment : BaseFragment(R.layout.fragment_device_details) {

    private val viewModel by viewModels<DeviceDetailsViewModel>()

    private var refreshFilters = false
    private var deviceId: String? = null

    val args: DeviceDetailsFragmentArgs by navArgs()

    private var deviceInfo: Device? = null

    override val showBottomNavigationView = true

    // initial value set to 180 because image needs to be inverted
    private var angle = 180f
    private var filtersExpanded = false

    override fun callOperations() {

    }

    override fun onSetupLayout(savedInstanceState: Bundle?) {
        buttonRetry.setOnClickListener {
            makeDevicesRequest()
        }
    }

    override fun onBindViewModel() {
        makeDevicesRequest()

        buttonTakeDevice.setOnClickListener {
            viewModel.takeDevice()
        }
        buttonReturnDevice.setOnClickListener {
            viewModel.returnDevice()
        }

        swipeRefreshLayout.setOnRefreshListener {
            makeDevicesRequest()
        }

        deviceId = args.deviceId
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
                    .setTitle("Устройство успешно взято")
                    .setPositiveButton("Ok", null)
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
                    .setTitle("Устройство успешно сдано")
                    .setPositiveButton("Ok", null)
                    .setOnDismissListener {
                        makeDevicesRequest()
                    }
                    .show()
            }
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
                buttonTakeDevice.text = "ВЗЯТЬ"
            }
            OpState.FAILURE -> {
                progressBarTake.isGone = true
                buttonTakeDevice.isClickable = true
                buttonTakeDevice.text = "ВЗЯТЬ"
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
                buttonReturnDevice.text = "ВЗЯТЬ"
            }
            OpState.FAILURE -> {
                progressBarReturn.isGone = true
                buttonReturnDevice.isClickable = true
                buttonReturnDevice.text = "ВЗЯТЬ"
            }
        }
    }

    private fun makeDevicesRequest() {
        viewModel.getDevices()
    }

    private fun handleInfo(userList: List<User>, deviceList: List<Device>) {
        deviceInfo = deviceList.filter { deviceId == it.id.toString() }.first()
        deviceInfo?.let { deviceInfo ->
            //required fields
            if (deviceInfo.osType.toLowerCase() == "android") {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_android_robot)
            } else {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_apple_logo_grey)
            }

            textViewDeviceName.text = deviceInfo.name
            textViewDeviceOS.text = "${deviceInfo.osType} ${deviceInfo.osVersion}"
            textViewDeviceResolution.text = deviceInfo.resolution
            textViewDeviceType.text = deviceInfo.type

            // optional fields
            if(deviceInfo.nickname.isNotEmpty()) {
                textViewDeviceNickname.text = deviceInfo.nickname
                textViewDeviceNickname.isVisible = true
                textViewDeviceNicknameTitle.isVisible = true
            } else {
                textViewDeviceNickname.isGone = true
                textViewDeviceNicknameTitle.isGone = true
            }

            if(deviceInfo.shell.isNotEmpty()) {
                textViewDeviceShell.text = deviceInfo.shell
                textViewDeviceShell.isVisible = true
                textViewDeviceShellTitle.isVisible = true
            } else {
                textViewDeviceShell.isGone = true
                textViewDeviceShellTitle.isGone = true
            }

            if(deviceInfo.comment.isNotEmpty()) {
                textViewDeviceComment.text = deviceInfo.comment
                textViewDeviceComment.isVisible = true
                textViewDeviceCommentTitle.isVisible = true
            } else {
                textViewDeviceComment.isGone = true
                textViewDeviceCommentTitle.isGone = true
            }

            // check owner and create listener to open telegram
            if(deviceInfo.ownerId == 0) {
                textViewDeviceStatus.text = "Свободен"
                buttonTakeDevice.isEnabled = true
                textViewDeviceStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.soft_green,
                        null
                    )
                )
                imageViewTelegramIcon.isVisible = false
                textViewDeviceStatus.isClickable = false
                buttonTakeDevice.isEnabled = true
                buttonReturnDevice.isEnabled = false
            } else {
                val owner = userList.first { deviceInfo.ownerId == it.id }
                if(owner.id == viewModel.originId) {
                    textViewDeviceStatus.text = "Находится в Вашем пользовании"
                    imageViewTelegramIcon.isVisible = false
                    textViewDeviceStatus.isClickable = false
                    buttonReturnDevice.isEnabled = true
                } else {
                    textViewDeviceStatus.text = "Занят. Арендатор - ${owner.lastName} ${owner.firstName}"
                    imageViewTelegramIcon.isVisible = true
                    textViewDeviceStatus.isClickable = true
                    buttonReturnDevice.isEnabled = false

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

                textViewDeviceStatus.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.soft_red,
                        null
                    )
                )

                buttonTakeDevice.isEnabled = false

            }
        }

    }

}