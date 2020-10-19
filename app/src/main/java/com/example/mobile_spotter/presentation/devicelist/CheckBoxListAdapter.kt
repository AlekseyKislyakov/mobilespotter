package com.example.mobile_spotter.presentation.devicelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_spotter.R
import javax.inject.Inject

class CheckBoxListAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemList = mutableListOf<String>()
    var selectedSet = mutableSetOf<String>()

    var selectedItem = ""

    companion object {
        private const val DEFAULT_TYPE = 0
    }

    var onClickListener: (String) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringCheckboxViewHolder {
        return StringCheckboxViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_list_string_checkbox,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as StringCheckboxViewHolder).bind(itemList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return DEFAULT_TYPE
    }

    fun replaceItems(list: List<String>) {
        itemList.clear()
        itemList.addAll(list)
        this.notifyDataSetChanged()
    }

    fun setSelection(selectedList: List<String>) {
        itemList.forEach {
            if()
        }
        notifyDataSetChanged()
    }

    fun converterClicks(): Observable<Converter> = converterClicksRelay

    inner class StringCheckboxViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(converter: Converter) = with(itemView) {
            textViewConverterName.text = converter.title
            itemView.setOnClickListener {
                converterClicksRelay.accept(converter)
                converter.code?.let { it1 ->
                    selectedItem = it1
                    notifyDataSetChanged()
                }
            }
            radioButtonConverter.isChecked = converter.code == selectedItem
        }
    }
}
