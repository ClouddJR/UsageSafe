package com.clouddroid.usagesafe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.models.AppUsageInfo
import com.clouddroid.usagesafe.utils.PackageInfoUtils.getAppName
import com.clouddroid.usagesafe.utils.PackageInfoUtils.getRawAppIcon
import com.clouddroid.usagesafe.utils.TextUtils.getTotalScreenTimeText
import kotlinx.android.synthetic.main.item_most_used.view.*


class MostUsedAdapter(private val appUsageList: List<AppUsageInfo>) :
    RecyclerView.Adapter<MostUsedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_most_used, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appUsageList[position])
    }

    override fun getItemCount() = appUsageList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appUsageInfo: AppUsageInfo) {
            itemView.screenTimeTV.text = itemView.context.getString(
                R.string.most_used_screen_time,
                getTotalScreenTimeText(appUsageInfo.totalTimeInForeground, itemView.context)
            )

            itemView.launchCountTV.text = itemView.context.getString(
                R.string.most_used_opened_times,
                appUsageInfo.launchCount
            )

            itemView.appIconIV.setImageDrawable(getRawAppIcon(appUsageInfo.packageName, itemView.context))

            itemView.appTitleTV.text = getAppName(appUsageInfo.packageName, itemView.context)
        }

    }
}