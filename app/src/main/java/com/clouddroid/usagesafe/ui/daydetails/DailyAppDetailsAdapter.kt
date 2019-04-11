package com.clouddroid.usagesafe.ui.daydetails

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsActivity
import com.clouddroid.usagesafe.util.PackageInfoUtils.getAppName
import com.clouddroid.usagesafe.util.PackageInfoUtils.getRawAppIcon
import com.clouddroid.usagesafe.util.TextUtils.getTotalScreenTimeText
import kotlinx.android.synthetic.main.item_most_used.view.*


class DailyAppDetailsAdapter(private val appUsageList: MutableList<AppUsageInfo>) :
    RecyclerView.Adapter<DailyAppDetailsAdapter.ViewHolder>() {

    fun addItems(appsList: List<AppUsageInfo>) {
        appUsageList.addAll(appsList)
        notifyItemRangeInserted(5, appsList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_most_used, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appUsageList[position])
    }

    override fun getItemCount() = appUsageList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appUsageInfo: AppUsageInfo) {

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, AppDetailsActivity::class.java)
                intent.putExtra(AppDetailsActivity.PACKAGE_NAME_KEY, appUsageInfo.packageName)
                itemView.context.startActivity(intent)
            }

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