package com.example.mobile_spotter.presentation.devicelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.ext.*
import kotlinx.android.synthetic.main.item_device_list.view.*
import java.util.*
import javax.inject.Inject

class DeviceListAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val DEVICE_VIEW_TYPE = 0
    }

    private val deviceData = mutableListOf<FullDeviceInfo>()

    private val filteredDevices = mutableListOf<FullDeviceInfo>()

    var onClickListener: (Device) -> Unit = {}
    var onEmptyListAction: (Boolean) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            DEVICE_VIEW_TYPE -> return DeviceViewHolder(
                    inflater
                            .inflate(R.layout.item_device_list, parent, false)
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

    override fun getItemCount(): Int {
        return if (filteredDevices.isEmpty()) 0 else filteredDevices.size
    }

    override fun getItemViewType(position: Int): Int {
        return DEVICE_VIEW_TYPE
    }

    fun applyFilter(filter: DeviceFilter, query: String) {
        if(deviceData.isNotEmpty()) {
            onEmptyListAction.invoke(false)
        }

        filteredDevices.clear()
        filteredDevices.addAll(deviceData.asSequence()
                .filter { filter.os == OS_ALL || it.device.osType.toLowerCase(Locale.ROOT) == filter.os }
                .filter { filter.selectedResolutionSet.contains(it.device.detailedResolution()) }
                .filter { filter.selectedVersionSet.contains(it.device.detailedVersion()) }
                .filter { !filter.onlyAvailable || it.owner == null }
                .filter { !filter.nonPrivate || !it.device.private }.toList()
                .filter { it.device.name.containsNoCase(query) || it.device.nickname.containsNoCase(query) })

        if(filteredDevices.isEmpty() && deviceData.isNotEmpty()) {
            onEmptyListAction.invoke(true)
        }
        notifyDataSetChanged()
    }

    fun applyData(users: List<User>, devices: List<Device>) {
        deviceData.clear()
        devices.forEach { device ->
            deviceData.add(
                    FullDeviceInfo(
                            device,
                            users.filter { user -> user.id == device.ownerId }.firstOrNull()
                    )
            )
        }
        handleData()
    }

    private fun handleData() {
        filteredDevices.clear()
        filteredDevices.addAll(deviceData)

        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewDeviceName = itemView.textViewDeviceName
        private val textViewVersion = itemView.textViewDeviceVersion
        private val textViewStatus = itemView.textViewStatus

        private val imageViewDeviceAvatar = itemView.imageViewDeviceAvatar

        fun bind(data: FullDeviceInfo) {
            textViewDeviceName.text = data.device.detailedName()
            textViewVersion.text = data.device.fullVersion()

            if (data.device.osType.toLowerCase() == OS_ANDROID) {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_android_robot)
            } else {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_apple_logo_grey)
            }

            if (data.owner == null) {
                textViewStatus.text = itemView.context.getString(R.string.device_list_free)
                textViewStatus.setTextColor(
                        ResourcesCompat.getColor(
                                itemView.resources,
                                R.color.soft_green,
                                null
                        )
                )
            } else {
                textViewStatus.text =
                    itemView.context.getString(R.string.device_list_busy, data.owner.fullName())
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

