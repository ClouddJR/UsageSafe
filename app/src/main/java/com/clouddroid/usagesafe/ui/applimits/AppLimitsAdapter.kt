package com.clouddroid.usagesafe.ui.applimits

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppDetails
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_app_limit.view.*
import kotlinx.android.synthetic.main.item_apps_limit_header.view.*


class AppLimitsAdapter(
    private val appsList: MutableList<AppDetails>,
    val context: Context,
    private val onAppLimitButtonClick: (packageName: String) -> Unit,
    private val onScreenLimitButtonClick: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val headerType = 0
    private val itemType = 1

    fun swapItems(appsList: List<AppDetails>) {
        var disposable = Observable.fromCallable {
            DiffUtil.calculateDiff(
                AppLimitsAdapterDiffUtil(
                    this@AppLimitsAdapter.appsList,
                    appsList
                )
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.appsList.clear()
                this.appsList.addAll(appsList)

                it.dispatchUpdatesTo(this)
            }
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

            itemView.setLimitBT.setOnClickListener {
                onAppLimitButtonClick.invoke(appDetails.packageName)
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            itemView.ovScreenTimeTitleTV.setOnClickListener {
                onScreenLimitButtonClick.invoke()
            }
        }

    }
}