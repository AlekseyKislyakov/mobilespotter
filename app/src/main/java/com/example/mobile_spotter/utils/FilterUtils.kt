package com.example.mobile_spotter.utils

import com.example.mobile_spotter.data.entities.DeviceFilter
import com.example.mobile_spotter.data.entities.FullDeviceInfo
import com.example.mobile_spotter.data.entities.ORDERING_AS_IS
import com.example.mobile_spotter.data.entities.ORDERING_DECREASING
import com.example.mobile_spotter.data.entities.OS_ALL
import com.example.mobile_spotter.ext.containsNoCase
import com.example.mobile_spotter.ext.detailedResolution
import com.example.mobile_spotter.ext.detailedVersion
import com.example.mobile_spotter.ext.splitAndCompareVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import java.util.Locale

suspend fun applyFilterOnBackground(
    deviceData: List<FullDeviceInfo>,
    filter: DeviceFilter,
    currentUserId: Int,
    query: String
): List<FullDeviceInfo> = withContext(context = Dispatchers.IO) {
        var resultData = deviceData.asSequence()
            .filter { filter.os == OS_ALL || it.device.osType.toLowerCase(Locale.ROOT) == filter.os }
            .filter { filter.selectedResolutionSet.contains(it.device.detailedResolution()) }
            .filter { filter.selectedVersionSet.contains(it.device.detailedVersion()) }
            .filter { !filter.onlyAvailable || it.owner == null }
            .filter { !filter.nonPrivate || !it.device.private }.toList()
            .filter { !filter.onlyMine || it.owner?.id == currentUserId }
            .filter { it.device.name.containsNoCase(query) || it.device.nickname.containsNoCase(query) }

        if (filter.orderingMain != ORDERING_AS_IS) {
            resultData = resultData.sortedWith { o2, o1 ->
                if (o1.device.osType != o2.device.osType) {
                    o1.device.osType.compareTo(o2.device.osType)
                } else {
                    splitAndCompareVersion(o1.device.osVersion, o2.device.osVersion)
                }
            }
        }
        if (filter.orderingAlphabetical == ORDERING_DECREASING) {
            resultData = resultData.reversed()
        }
        resultData
}