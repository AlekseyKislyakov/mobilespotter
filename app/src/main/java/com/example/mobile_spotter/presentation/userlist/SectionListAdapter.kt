package com.example.mobile_spotter.presentation.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.ALL
import com.example.mobile_spotter.data.entities.Section
import com.example.mobile_spotter.data.entities.departmentPriority
import kotlinx.android.synthetic.main.item_filter_with_stroke.view.*
import javax.inject.Inject

class SectionListAdapter @Inject constructor() :
        RecyclerView.Adapter<SectionViewHolder>() {

    val items = mutableListOf<Section>()

    var doOnSectionClicked: (Section) -> Unit = {
        if (it.id == ALL) {
            items.forEach { item -> item.isSelected = false }
        } else {
            items.firstOrNull { it.id == ALL }?.isSelected = false
        }
        it.isSelected = !it.isSelected
        notifyDataSetChanged()

        onSectionListChangeListener.invoke(items)
    }

    var onSectionListChangeListener: (List<Section>) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        return SectionViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_filter_with_stroke, parent, false)
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        holder.bind(items[position], doOnSectionClicked)
    }

    fun setItems(newItems: List<Section>) {
        val filteredItems = newItems.toSet()
        departmentPriority.forEach { department ->
            filteredItems.forEach { section ->
                if (department.toString() == section.id) {
                    items.add(section)
                }
            }
        }
        newItems.firstOrNull().let {
            if (it?.id == ALL) {
                items.add(0, newItems.first())
            }
        }

        notifyDataSetChanged()
        preselectSection()
    }

    fun setCurrentSection(section: Section) {
        doOnSectionClicked.invoke(section)
    }

    private fun preselectSection() {
        if (items.none { it.isSelected }) {
            items.firstOrNull()?.let {
                doOnSectionClicked(it)
            }
        }
    }
}

class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(
        section: Section,
        doOnClick: (Section) -> Unit
    ) = with(itemView) {
        textViewFilter.isSelected = section.isSelected
        textViewFilter.text = section.name
        setOnClickListener {
            doOnClick(section)
        }

    }
}
