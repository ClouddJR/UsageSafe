package com.clouddroid.usagesafe.ui.applimits.focus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppDetails
import com.clouddroid.usagesafe.data.model.FocusModeApp
import kotlinx.android.synthetic.main.item_app_info_focus.view.*
import kotlinx.android.synthetic.main.item_apps_list_focus_header.view.*

class FocusAppListAdapter(
    private val focusModeAppsList: List<FocusModeApp>,
    appsList: List<AppDetails>,
    private val checkBoxListener: (AppDetails) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val includedApps = mutableListOf<AppDetails>()
    private val notIncludedApps = mutableListOf<AppDetails>()

    private val headerType = 0
    private val itemType = 1

    init {
        appsList.map { app ->
            if (focusModeAppsList.any { focusApp -> focusApp.packageName == app.packageName }) app.isInFocusMode = true
        }

        includedApps.addAll(appsList.filter { it.isInFocusMode })
        notIncludedApps.addAll(appsList.filter { !it.isInFocusMode })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == headerType) {
            HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_apps_list_focus_header,
                    parent,
                    false
                )
            )
        } else {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_app_info_focus,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                if (position <= includedApps.size) {
                    holder.bind(includedApps[position - 1])
                } else {
                    holder.bind(notIncludedApps[position - includedApps.size - 2])
                }
            }
            is HeaderViewHolder -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = includedApps.size + notIncludedApps.size + 2

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 || position == includedApps.size + 1) {
            headerType
        } else {
            itemType
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appDetails: AppDetails) {
            Glide.with(itemView.context).load(appDetails.icon).into(itemView.appIconIV)
            itemView.appTitle.text = appDetails.name

            itemView.focusModeCB.setOnCheckedChangeListener(null)
            itemView.focusModeCB.isChecked = appDetails.isInFocusMode

            itemView.focusModeCB.setOnCheckedChangeListener { _, isChecked ->
                var insertIndex: Int
                appDetails.isInFocusMode = isChecked

                if (isChecked) {
                    notIncludedApps.removeAt(adapterPosition - includedApps.size - 2)
                    insertIndex = findInsertIndexFor(appDetails, includedApps)
                    includedApps.add(insertIndex, appDetails)
                    insertIndex += 1
                } else {
                    includedApps.removeAt(adapterPosition - 1)
                    insertIndex = findInsertIndexFor(appDetails, notIncludedApps)
                    notIncludedApps.add(insertIndex, appDetails)
                    insertIndex += 2 + includedApps.size
                }
                notifyItemMoved(adapterPosition, insertIndex)
                checkBoxListener.invoke(appDetails)
            }
        }

    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(position: Int) {
            itemView.sectionTitleTV.text = if (position == 0) {
                "Apps to be blocked during focus mode"
            } else {
                "Apps not affected by focus mode"
            }
        }

    }

    private fun findInsertIndexFor(appDetails: AppDetails, apps: MutableList<AppDetails>): Int {
        var insertIndex = 0

        //for every existing app in the list
        for (i in 0 until apps.size) {
            val ad = apps[i]

            //compare apps names alphabetically
            //and get the correct index inside the list where the app should be placed
            if (appDetails.name.toLowerCase() < ad.name.toLowerCase()) {
                insertIndex = i
                break
            }

            //insert as the last item if it reaches the end of the list
            if (i == apps.size - 1) {
                insertIndex = apps.size
            }
        }
        return insertIndex
    }
}
