package com.example.mobile_spotter.presentation.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandongogetap.stickyheaders.exposed.StickyHeader
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.*
import com.example.mobile_spotter.ext.containsNoCase
import kotlinx.android.synthetic.main.item_userlist_header.view.*
import kotlinx.android.synthetic.main.item_userlist_person.view.*
import java.util.*
import javax.inject.Inject

class UserListAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
        StickyHeaderHandler {

    companion object {

        private const val HEADER_VIEW_TYPE = 0
        private const val USER_VIEW_TYPE = 1
    }

    private val allUsers = mutableListOf<User>()
    private val items = mutableListOf<UserEntity>()

    private val sectionList = mutableListOf<Section>()

    var onUserClickListener: (User) -> Unit = {}
    var onEmptyListAction: (Boolean) -> Unit = {}

    var currentQuery = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            HEADER_VIEW_TYPE -> return DepartmentViewHolder(
                    inflater
                            .inflate(R.layout.item_userlist_header, parent, false)
            )
            USER_VIEW_TYPE -> return UserViewHolder(
                    inflater
                            .inflate(R.layout.item_userlist_person, parent, false)
            )
        }
        throw IllegalStateException("Unknown view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            HEADER_VIEW_TYPE -> (holder as DepartmentViewHolder).bind(
                    department = (getUserByAdapterPosition(position) as Department).department,
            )
            USER_VIEW_TYPE -> (holder as UserViewHolder).bind(
                    user = (getUserByAdapterPosition(position) as User)
            )
        }
    }

    private fun getUserByAdapterPosition(position: Int): UserEntity {
        return items[position]
    }

    override fun getItemCount(): Int {
        return if (items.isEmpty()) 0 else items.size
    }

    override fun getAdapterData(): MutableList<*> {
        return items
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is Department) {
            HEADER_VIEW_TYPE
        } else {
            USER_VIEW_TYPE
        }
    }

    /**
     *   Here the filters are applied to initial data set
     *
     *   @param query is taken from the searchview and controls the data set in such ways:
     *   empty query -> default data set is applied
     *   non-empty   -> data is filtered with no case sensitive
     *
     *   @param sections is taken from sectionsAdapter action and applied this way:
     *   1) section list is filtered to contain only selected elements
     *   2) section list is mapped to list of departments
     *   3) section department list looks for user.department whether it is in the list
     *   4) if section list is empty, all data is applied
     */


    fun applyFilters(query: String, sections: List<Section>? = null) {
        currentQuery = query
        if (query.isNotEmpty()) {
            handleData(allUsers
                    .filter { it.firstName.containsNoCase(query) || it.lastName.toLowerCase(Locale.ROOT).containsNoCase(query) }
                    .filter { user -> sections?.filter { it.isSelected }?.contains(user.department) ?: true })
        } else {
            handleData(allUsers
                    .filter { sections?.filter { it.isSelected }?.map { it.id }?.contains(it.department.toString()) ?: true })
        }
    }

    fun applySectionList(sections: List<Section>) {
        if (sections.isNotEmpty()) {
            if (sections.firstOrNull { it.id == ALL }?.isSelected == true) {
                applyFilters(currentQuery)
            } else {
                applyFilters(currentQuery, sections)
            }
        }
    }

    fun applyData(list: List<User>) {
        allUsers.addAll(list)
        handleData(list)
    }

    private fun handleData(list: List<User>) {
        onEmptyListAction.invoke(false)
        items.clear()
        departmentPriority.forEach { department ->
            list.filter { departmentPriority.contains(it.department) }
                    .groupBy { it.department }
                    .forEach { entry ->
                        if (department == entry.key && entry.value.isNotEmpty()) {
                            items.add(Department(entry.value.first().department))
                            items.addAll(entry.value.map { it })
                        }
                    }
        }

        if (items.isEmpty() && allUsers.isNotEmpty()) {
            onEmptyListAction.invoke(true)
        }

        notifyDataSetChanged()

    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewName = itemView.textViewUserFirstName
        private val textViewLastName = itemView.textViewUserLastName

        fun bind(user: User) {
            textViewName.text = user.firstName
            textViewLastName.text = user.lastName

            itemView.setOnClickListener {
                onUserClickListener.invoke(user)
            }
        }
    }

    inner class DepartmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), StickyHeader {

        private val textViewDepartment = itemView.textViewDepartment

        fun bind(department: Int) {
            textViewDepartment.text = itemView.resources.getString(department.recognize())
        }
    }
}

