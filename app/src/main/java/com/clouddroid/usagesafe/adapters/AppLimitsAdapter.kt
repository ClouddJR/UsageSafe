package com.clouddroid.usagesafe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.models.AppDetails
import kotlinx.android.synthetic.main.item_app_limit.view.*

class AppLimitsAdapter(private val appsList: MutableList<AppDetails>, val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val headerType = 0
    private val itemType = 1

    fun setItems(appsList: List<AppDetails>) {
        this.appsList.addAll(appsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == headerType) {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_apps_limit_header, parent, false)
            HeaderViewHolder(itemView)
        } else {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_app_limit, parent, false)
            ItemViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            holder.bind(appsList[position - 1])
        } else if (holder is HeaderViewHolder) {
            holder.bind()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            headerType
        } else {
            itemType
        }
    }

    override fun getItemCount() = appsList.size + 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appDetails: AppDetails) {

            Glide.with(context).load(appDetails.icon).into(itemView.appIconIV)
            itemView.appTitleTV.text = appDetails.name

        }

    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {

        }

    }
}