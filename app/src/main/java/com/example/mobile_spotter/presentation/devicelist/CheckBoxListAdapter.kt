package com.example.mobile_spotter.presentation.devicelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.OS_ALL
import com.example.mobile_spotter.data.entities.OS_ANDROID
import com.example.mobile_spotter.data.entities.OS_IOS
import kotlinx.android.synthetic.main.item_list_string_checkbox.view.*
import java.util.*
import javax.inject.Inject

class CheckBoxListAdapter @Inject constructor() :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemSet = mutableSetOf<String>()
    var selectedSet = mutableSetOf<String>()

    companion object {
        private const val DEFAULT_TYPE = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringCheckboxViewHolder {
        return StringCheckboxViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_list_string_checkbox,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int = itemSet.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as StringCheckboxViewHolder).bind(itemSet.elementAt(position))
    }

    override fun getItemViewType(position: Int): Int {
        return DEFAULT_TYPE
    }

    fun replaceItems(list: List<String>, selected: List<String>, type: String? = null) {
        if (type != null) {
            itemSet.clear()
            selectedSet.clear()
            when (type) {
                OS_ALL -> {
                    itemSet.addAll(list)
                    selectedSet.addAll(selected)
                }
                else -> {
                    itemSet.addAll(list.filter { it.toLowerCase(Locale.getDefault()).contains(type) })
                    selectedSet.addAll(selected.filter { it.toLowerCase(Locale.ROOT).contains(type) })
                }
            }
        }
        notifyDataSetChanged()
    }

    inner class StringCheckboxViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: String) = with(itemView) {
            checkBoxListItem.text = item
            checkBoxListItem.isChecked = selectedSet.contains(item)

            checkBoxListItem.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedSet.add(item)
                } else {
                    selectedSet.remove(item)
                }
            }
        }
    }
}
