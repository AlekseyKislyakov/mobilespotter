package com.example.mobile_spotter.presentation.devicelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.ext.*
import kotlinx.android.synthetic.main.item_device_list_row.view.*
import kotlinx.android.synthetic.main.item_device_list_tile.view.*
import java.util.*
import javax.inject.Inject

class DeviceListAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val DEVICE_VIEW_TYPE_TILE = 0
        private const val DEVICE_VIEW_TYPE_ROW = 1
    }

    private val deviceData = mutableListOf<FullDeviceInfo>()

    private val filteredDevices = mutableListOf<FullDeviceInfo>()

    private var currentQuery = ""
    private var currentFilter = DeviceFilter()

    var mode = DeviceListMode.ROW
    var currentUserId = 0

    var onClickListener: (Device) -> Unit = {}
    var onItemSelected: (Device?) -> Unit = {}
    var onItemUnselected: (Device?) -> Unit = {}
    var onEmptyListAction: (Boolean) -> Unit = {}

    var selectedCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            DEVICE_VIEW_TYPE_TILE -> return DeviceTileViewHolder(
                    inflater.inflate(R.layout.item_device_list_tile, parent, false)
            )
            DEVICE_VIEW_TYPE_ROW -> return DeviceRowViewHolder(
                    inflater.inflate(R.layout.item_device_list_row, parent, false)
            )
        }
        throw IllegalStateException("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (mode) {
            DeviceListMode.TILE -> (holder as DeviceTileViewHolder).bind(filteredDevices[position])
            DeviceListMode.ROW -> (holder as DeviceRowViewHolder).bind(filteredDevices[position])
        }
    }

    override fun getItemCount(): Int {
        return if (filteredDevices.isEmpty()) 0 else filteredDevices.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(mode) {
            DeviceListMode.TILE -> DEVICE_VIEW_TYPE_TILE
            DeviceListMode.ROW -> DEVICE_VIEW_TYPE_ROW
        }
    }

    fun applyFilter(filter: DeviceFilter, query: String) {
        currentFilter = filter
        currentQuery = query

        if (deviceData.isNotEmpty()) {
            onEmptyListAction.invoke(false)
        }

        filteredDevices.clear()
        filteredDevices.addAll(deviceData.asSequence()
                .filter { filter.os == OS_ALL || it.device.osType.toLowerCase(Locale.ROOT) == filter.os }
                .filter { filter.selectedResolutionSet.contains(it.device.detailedResolution()) }
                .filter { filter.selectedVersionSet.contains(it.device.detailedVersion()) }
                .filter { !filter.onlyAvailable || it.owner == null }
                .filter { !filter.nonPrivate || !it.device.private }.toList()
                .filter { !filter.onlyMine || it.owner?.id == currentUserId }
                .filter { it.device.name.containsNoCase(query) || it.device.nickname.containsNoCase(query) })
        clearSelection()
        if (filteredDevices.isEmpty() && deviceData.isNotEmpty()) {
            onEmptyListAction.invoke(true)
        }
        notifyDataSetChanged()
    }

    fun applyData(users: List<User>, devices: List<Device>) {
        deviceData.clear()
        devices.forEach { device ->
            deviceData.add(FullDeviceInfo(device, users.firstOrNull { user -> user.id == device.ownerId }))
        }
        handleData()
    }

    fun clearSelection() {
        filteredDevices.forEach {
            it.selected = false
        }
        selectedCount = 0
        notifyDataSetChanged()
        onItemSelected.invoke(null)
    }

    fun getSelectedDevices(): List<FullDeviceInfo> {
        return filteredDevices.filter { it.selected }
    }

    fun selectByCode(device: Device) {
        filteredDevices.filter { it.device == device }.forEach {
            if (!it.selected) {
                it.selected = true
                selectedCount++
                onItemSelected.invoke(device)
            }
        }
        notifyDataSetChanged()
    }

    private fun handleData() {
        filteredDevices.clear()
        filteredDevices.addAll(deviceData)
        applyFilter(currentFilter, currentQuery)

        notifyDataSetChanged()
    }

    inner class DeviceTileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewDeviceName = itemView.textViewDeviceNameTile
        private val textViewVersion = itemView.textViewDeviceVersionTile
        private val textViewStatus = itemView.textViewStatusTile

        private val cardDeviceList = itemView.cardDeviceListTile

        private val imageViewDeviceAvatar = itemView.imageViewDeviceAvatarRow

        fun bind(data: FullDeviceInfo) {
            if (data.selected) {
                cardDeviceList.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.snow_flurry))
            } else {
                cardDeviceList.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            textViewDeviceName.text = data.device.detailedName(itemView.context)
            textViewVersion.text = data.device.fullVersion(itemView.context)

            if (data.device.osType.toLowerCase(Locale.getDefault()) == OS_ANDROID) {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_android_robot)
            } else {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_apple_logo_grey)
            }

            if (data.owner == null) {
                textViewStatus.text = itemView.context.getString(R.string.device_list_free)
                textViewStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.soft_green,null))
            } else {
                textViewStatus.text = itemView.context.getString(R.string.device_list_busy, data.owner.fullName())
                textViewStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.soft_red,null))
            }

            itemView.setOnClickListener {
                if (selectedCount > 0) {
                    setSelection(data)
                } else {
                    onClickListener.invoke(data.device)
                }
            }

            itemView.setOnLongClickListener {
                setSelection(data)
                true
            }
        }

        private fun setSelection(data: FullDeviceInfo) {
            if (!data.selected) {
                selectedCount++
                onItemSelected.invoke(data.device)
            } else {
                selectedCount--
                onItemUnselected.invoke(data.device)
            }
            data.selected = !data.selected
            notifyDataSetChanged()
        }
    }

    inner class DeviceRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewDeviceName = itemView.textViewDeviceNameRow
        private val textViewVersion = itemView.textViewDeviceVersionRow
        private val textViewStatus = itemView.textViewStatusRow

        private val cardDeviceList = itemView.cardDeviceListRow

        private val imageViewDeviceAvatar = itemView.imageViewDeviceAvatarRow

        fun bind(data: FullDeviceInfo) {
            if (data.selected) {
                cardDeviceList.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.snow_flurry))
            } else {
                cardDeviceList.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            textViewDeviceName.text = data.device.detailedName(itemView.context)
            textViewVersion.text = data.device.detailedVersion()

            if (data.device.osType.toLowerCase(Locale.getDefault()) == OS_ANDROID) {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_android_robot)
            } else {
                imageViewDeviceAvatar.setImageResource(R.drawable.ic_apple_logo_grey)
            }

            if (data.owner == null) {
                textViewStatus.text = itemView.context.getString(R.string.device_list_free)
                textViewStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.soft_green,null))
            } else {
                textViewStatus.text = itemView.context.getString(R.string.device_list_busy, data.owner.fullName())
                textViewStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.soft_red,null))
            }

            itemView.setOnClickListener {
                if (selectedCount > 0) {
                    setSelection(data)
                } else {
                    onClickListener.invoke(data.device)
                }
            }

            itemView.setOnLongClickListener {
                setSelection(data)
                true
            }
        }

        private fun setSelection(data: FullDeviceInfo) {
            if (!data.selected) {
                selectedCount++
                onItemSelected.invoke(data.device)
            } else {
                selectedCount--
                onItemUnselected.invoke(data.device)
            }
            data.selected = !data.selected
            notifyDataSetChanged()
        }
    }

    enum class DeviceListMode {
        TILE,
        ROW
    }
}

