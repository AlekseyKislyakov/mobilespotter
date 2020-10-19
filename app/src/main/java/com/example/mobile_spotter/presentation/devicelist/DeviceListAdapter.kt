package com.example.mobile_spotter.presentation.devicelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.brandongogetap.stickyheaders.exposed.StickyHeader
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import kotlinx.android.synthetic.main.item_devicelist.view.*
import kotlinx.android.synthetic.main.item_userlist_header.view.*
import kotlinx.android.synthetic.main.item_userlist_person.view.*
import java.util.*
import javax.inject.Inject

class DeviceListAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val DEVICE_VIEW_TYPE = 0
//        private const val HEADER_VIEW_TYPE = 0
//        private const val USER_VIEW_TYPE = 1
//
//        private val departmentPriority = listOf(
//            QA,
//            ANDROID,
//            IOS,
//            HEAD,
//            BACKEND_JAVA,
//            BACKEND_PHP,
//            BACKEND_PYTHON,
//            FRONTEND_JS,
//            FRONTEND_TCS,
//            SERVICE
//        )
    }

    private val allUsers = mutableListOf<User>()
    private val allDevices = mutableListOf<Device>()

    private val filteredDevices = mutableListOf<FullDeviceInfo>()

    var onClickListener: (Device) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
//            HEADER_VIEW_TYPE -> return DepartmentViewHolder(
//                inflater
//                    .inflate(R.layout.item_userlist_header, parent, false)
//            )
            DEVICE_VIEW_TYPE -> return DeviceViewHolder(
                inflater
                    .inflate(R.layout.item_devicelist, parent, false)
            )
        }
        throw IllegalStateException("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            DEVICE_VIEW_TYPE -> (holder as DeviceViewHolder).bind(
                filteredDevices[position]
            )
        }
    }

    private fun getUserByAdapterPosition(position: Int): FullDeviceInfo {
        return filteredDevices[position]
    }

    override fun getItemCount(): Int {
        return if (filteredDevices.isEmpty()) 0 else filteredDevices.size
    }

    override fun getItemViewType(position: Int): Int {
        return DEVICE_VIEW_TYPE
    }

    fun applyQuery(query: String) {
//        if (query.isNotEmpty()) {
////            handleData(allUsers.filter {
////                it.firstName.toLowerCase(Locale.ROOT).contains(query) ||
////                        it.lastName.toLowerCase(Locale.ROOT).contains(query)
////            })
//        } else {
//            handleData(allUsers)
//        }
    }

    fun applyData(users: List<User>, devices: List<Device>) {
        allUsers.addAll(users)
        allDevices.addAll(devices)
        handleData()
    }

    private fun handleData() {
        filteredDevices.clear()
        allDevices.forEach { device ->
            filteredDevices.add(
                FullDeviceInfo(
                    device,
                    allUsers.filter { user -> user.id == 345 }.firstOrNull()
                )
            )
        }

        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewDeviceName = itemView.textViewDeviceName
        private val textViewVersion = itemView.textViewDeviceVersion
        private val textViewStatus = itemView.textViewStatus

        private val imageViewDeviceAvatar = itemView.imageViewDeviceAvatar

        fun bind(data: FullDeviceInfo) {
            textViewDeviceName.text = "${data.device.name} (${data.device.nickname})"
            textViewVersion.text =
                "${data.device.osType} ${data.device.osVersion} ${data.device.resolution}"

            if (data.device.osType.toLowerCase() == "android") {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_android_robot)
            } else {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_apple_logo_grey)
            }

            if (data.owner == null) {
                textViewStatus.text = "Свободен"
                textViewStatus.setTextColor(
                    ResourcesCompat.getColor(
                        itemView.resources,
                        R.color.soft_green,
                        null
                    )
                )
            } else {
                textViewStatus.text =
                    "Занят. Арендатор - ${data.owner.lastName} ${data.owner.firstName}"
                textViewStatus.setTextColor(
                    ResourcesCompat.getColor(
                        itemView.resources,
                        R.color.soft_red,
                        null
                    )
                )
            }

            itemView.setOnClickListener {
                onClickListener.invoke(data.device)
            }
        }
    }
}

