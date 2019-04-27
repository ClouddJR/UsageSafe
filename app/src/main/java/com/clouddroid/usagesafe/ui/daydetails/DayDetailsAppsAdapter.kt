package com.clouddroid.usagesafe.ui.daydetails

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsActivity
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.TextUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_app_usage_details.view.*

class DayDetailsAppsAdapter(private val appsUsageList: MutableList<AppUsageInfo>) :
    RecyclerView.Adapter<DayDetailsAppsAdapter.ViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_app_usage_details, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appsUsageList[position])
    }

    override fun getItemCount() = appsUsageList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appUsageInfo: AppUsageInfo) {

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, AppDetailsActivity::class.java)
                intent.putExtra(AppDetailsActivity.PACKAGE_NAME_KEY, appUsageInfo.packageName)
                itemView.context.startActivity(intent)
            }

            itemView.screenTimeTV.text = itemView.context.getString(
                R.string.most_used_screen_time,
                TextUtils.getTotalScreenTimeText(appUsageInfo.totalTimeInForeground, itemView.context)
            )

            itemView.launchCountTV.text = itemView.context.getString(
                R.string.most_used_opened_times,
                appUsageInfo.launchCount
            )

            itemView.appIconIV.setImageDrawable(
                PackageInfoUtils.getRawAppIcon(
                    appUsageInfo.packageName,
                    itemView.context
                )
            )

            itemView.appTitleTV.text = PackageInfoUtils.getAppName(appUsageInfo.packageName, itemView.context)
        }

    }
}