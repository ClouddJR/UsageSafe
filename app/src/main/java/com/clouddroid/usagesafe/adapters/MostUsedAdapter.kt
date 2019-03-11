package com.clouddroid.usagesafe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.models.AppUsageInfo
import com.clouddroid.usagesafe.utils.TextUtils.getTotalScreenTimeText
import kotlinx.android.synthetic.main.item_most_used.view.*


class MostUsedAdapter(private val appUsageList: List<AppUsageInfo>) :
    RecyclerView.Adapter<MostUsedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(com.clouddroid.usagesafe.R.layout.item_most_used, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appUsageList[position])
    }

    override fun getItemCount() = appUsageList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appUsageInfo: AppUsageInfo) {
            itemView.screenTimeTV.text = itemView.context.getString(
                com.clouddroid.usagesafe.R.string.most_used_screen_time,
                getTotalScreenTimeText(appUsageInfo.totalTimeInForeground)
            )

            itemView.launchCountTV.text = itemView.context.getString(
                com.clouddroid.usagesafe.R.string.most_used_opened_times,
                appUsageInfo.launchCount
            )

            itemView.appIconIV.setImageDrawable(
                itemView.context?.packageManager
                    ?.getApplicationIcon(appUsageInfo.packageName)
            )

            val applicationInfo = itemView.context?.packageManager?.getApplicationInfo(appUsageInfo.packageName, 0)

            val applicationName =
                if (applicationInfo != null) itemView.context?.packageManager?.getApplicationLabel(applicationInfo)
                else "(unknown)"

            itemView.appTitleTV.text = applicationName
        }

    }
}