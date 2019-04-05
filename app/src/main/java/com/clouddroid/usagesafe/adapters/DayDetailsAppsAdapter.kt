package com.clouddroid.usagesafe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.models.AppUsageInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class DayDetailsAppsAdapter(private val appsUsageList: MutableList<AppUsageInfo>) :
    RecyclerView.Adapter<AppUsageInfoAdapter.ViewHolder>() {

    private lateinit var disposable: Disposable

    fun swapItems(appsList: List<AppUsageInfo>) {
        //dispose previous calculation if any
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }

        disposable = Observable.fromCallable {
            androidx.recyclerview.widget.DiffUtil.calculateDiff(DiffUtil(appsUsageList, appsList))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.appsUsageList.clear()
                this.appsUsageList.addAll(appsList)

                it.dispatchUpdatesTo(this)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppUsageInfoAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_most_used, parent, false)
        return AppUsageInfoAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppUsageInfoAdapter.ViewHolder, position: Int) {
        holder.bind(appsUsageList[position])
    }

    override fun getItemCount() = appsUsageList.size

    inner class DiffUtil(private val oldList: List<AppUsageInfo>, private val newList: List<AppUsageInfo>) :
        androidx.recyclerview.widget.DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].totalTimeInForeground == newList[newItemPosition].totalTimeInForeground
                    && oldList[oldItemPosition].launchCount == newList[newItemPosition].launchCount
        }
    }
}