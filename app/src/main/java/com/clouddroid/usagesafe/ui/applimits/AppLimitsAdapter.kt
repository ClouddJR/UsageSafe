package com.clouddroid.usagesafe.ui.applimits

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.UsageSafeApp
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.TextUtils
import kotlinx.android.synthetic.main.item_app_limit.view.*


class AppLimitsAdapter(
    val context: Context
) :
    RecyclerView.Adapter<AppLimitsAdapter.ItemViewHolder>() {

    private var appsList = listOf<AppLimit>()
    private var usageMap = mapOf<String, AppUsageInfo>()

    fun replaceItems(list: List<AppLimit>) {
        appsList = list
        notifyDataSetChanged()
    }

    fun updateUsageProgressBars(map: Map<String, AppUsageInfo>) {
        usageMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_app_limit, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(appsList[position])
    }

    override fun getItemCount() = appsList.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(appLimit: AppLimit) {
            val appIcon = PackageInfoUtils.getRawAppIcon(appLimit.packageName, context)
            val appName = PackageInfoUtils.getAppName(appLimit.packageName, context)

            Glide.with(context).load(appIcon).into(itemView.adIcon)
            itemView.adHeadline.text = appName

            val currentLimitText =
                "${itemView.context.getString(R.string.item_app_limit_current_limit)} ${TextUtils.getAppLimitText(
                    appLimit.limit
                )}"

            itemView.currentLimitTV.text = currentLimitText

            itemView.setOnClickListener {
                displayEditOrDeleteDialog(itemView.context, appLimit)
            }

            val currentLimitPercentage =
                (usageMap[appLimit.packageName]?.totalTimeInForeground ?: 0).toDouble() / appLimit.limit
            itemView.progressBar.progress = (currentLimitPercentage * 100).toInt()
        }

        private fun displayEditOrDeleteDialog(context: Context, appLimit: AppLimit) {
            val databaseRepository = (context.applicationContext as UsageSafeApp).component.getDatabaseRepository()
            val dialog = AppLimitEditDialog(context, appLimit, databaseRepository)
            dialog.show()
        }
    }
}