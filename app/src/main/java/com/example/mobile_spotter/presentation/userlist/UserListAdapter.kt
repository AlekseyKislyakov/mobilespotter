package com.example.mobile_spotter.presentation.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.ANDROID
import com.example.mobile_spotter.data.entities.IOS
import com.example.mobile_spotter.data.entities.QA
import com.example.mobile_spotter.data.entities.User
import javax.inject.Inject

class UserListAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class AdapterItem private constructor(
        val user: User
    ) {

        companion object {
            fun createTopCity(user: User) = AdapterItem(user)
            fun createLetter(department: String) = AdapterItem(department)
        }

        enum class Type {
            TOP,
            NORMAL,
            LETTER
        }

        var last = false
    }

    companion object {

        private const val HEADER_VIEW_TYPE = 0
        private const val USER_VIEW_TYPE = 1

        private val departmentPriority = listOf(QA, ANDROID, IOS)
    }

    //    private var filteredItems = listOf<AdapterItem>()
//    private var sortedItems = listOf<AdapterItem>()
    private val items = mutableListOf<User>()
//        get() = if (filter.isEmpty()) sortedItems else filteredItems

//    var filter = ""
//        set(value) {
//            filteredItems = sortedItems
//                .filter { it.type == AdapterItem.Type.NORMAL }
//                .filter { it.city?.name?.contains(value, ignoreCase = true) ?: false }
//            field = value
//        }

    // private val clicksRelay = PublishRelay.create<City>().toSerialized()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            HEADER_VIEW_TYPE -> return TopViewHolder(
                inflater
                    .inflate(R.layout.item_cities_top, parent, false)
            )
            USER_VIEW_TYPE -> return NormalViewHolder(
                inflater
                    .inflate(R.layout.item_cities_normal, parent, false)
            )
        }
        throw IllegalStateException("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TOP_VIEW_TYPE -> (holder as TopViewHolder).bind(
                city = getCityByAdapterPosition(position),
                isLast = isLastPosition(position)
            )
            NORMAL_VIEW_TYPE -> (holder as NormalViewHolder).bind(
                city = getCityByAdapterPosition(position),
                isLast = isLastPosition(position)
            )
            LETTER_VIEW_TYPE -> (holder as LetterViewHolder).bind(
                getLetterByAdapterPosition(position)
            )
        }
    }

    private fun getCityByAdapterPosition(position: Int): City {
        return items[position].city ?: throw IllegalStateException("Unknown city position")
    }

    private fun getLetterByAdapterPosition(position: Int): Char {
        return items[position].letter ?: throw IllegalStateException("Unknown city position")
    }

    private fun isLastPosition(position: Int): Boolean {
        return items[position].last
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 1 else items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.isEmpty()) {
            EMPTY_VIEW_TYPE
        } else {
            when (items[position].type) {
                AdapterItem.Type.TOP -> TOP_VIEW_TYPE
                AdapterItem.Type.NORMAL -> NORMAL_VIEW_TYPE
                AdapterItem.Type.LETTER -> LETTER_VIEW_TYPE
            }
        }
    }

    fun setData(list: List<User>) {

        val result = mutableListOf<AdapterItem>()

        list.filter { departmentPriority.contains(it.department) }
            .groupBy { it.department }
            .forEach { entry ->


            }


        val top = topCities.mapNotNull { topId -> list.find { it.id == topId } }
        val sorted = list.sortedBy { it.name }
        val groups = sorted.groupBy { it.name.first().toUpperCase() }
        val alphabetic = groups.keys.sorted()

        top.forEach {
            result.add(AdapterItem.createTopCity(it))
        }
        result.last().last = true
        alphabetic.forEach {
            result.add(AdapterItem.createLetter(it))
            groups[it]?.forEach { result.add(AdapterItem.createNormalCity(it)) }
            result.last().last = true
        }

        this.sortedItems = result
    }

    fun clicks(): Observable<City> = clicksRelay

    inner class TopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val viewDivider = itemView.viewDivider
        private val textViewName = itemView.textViewName

        fun bind(city: City, isLast: Boolean) {
            textViewName.text = city.name
            viewDivider.visibility = if (!isLast) View.VISIBLE else View.GONE
            itemView.setOnClickListener { clicksRelay.accept(city) }
        }
    }

    inner class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val viewDivider = itemView.viewDivider
        private val textViewName = itemView.textViewName

        fun bind(city: City, isLast: Boolean) {
            textViewName.text = city.name
            viewDivider.visibility = if (!isLast) View.VISIBLE else View.GONE
            itemView.setOnClickListener { clicksRelay.accept(city) }
        }
    }

    inner class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewLetter = itemView.textViewLetter

        fun bind(letter: Char) {
            textViewLetter.text = letter.toString()
        }
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
