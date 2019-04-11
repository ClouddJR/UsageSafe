package com.clouddroid.usagesafe.ui.daydetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class DayDetailsAppsAdapter(private val appsUsageList: MutableList<AppUsageInfo>) :
    RecyclerView.Adapter<DailyAppDetailsAdapter.ViewHolder>() {

    private lateinit var disposable: Disposable

    fun swapItems(appsList: List<AppUsageInfo>) {
        //dispose previous calculation if exist
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }

        disposable = Observable.fromCallable {
            DiffUtil.calculateDiff(DayDetailsAdapterDiffUtil(appsUsageList, appsList))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.appsUsageList.clear()
                this.appsUsageList.addAll(appsList)

                it.dispatchUpdatesTo(this)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyAppDetailsAdapter.ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_most_used, parent, false)
        return DailyAppDetailsAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyAppDetailsAdapter.ViewHolder, position: Int) {
        holder.bind(appsUsageList[position])
    }

    override fun getItemCount() = appsUsageList.size
}