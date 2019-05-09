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
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.TextUtils
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_app_limit.view.*


class AppLimitsAdapter(
    private val appsList: OrderedRealmCollection<AppLimit>,
    val context: Context
) :
    RealmRecyclerViewAdapter<AppLimit, AppLimitsAdapter.ItemViewHolder>(appsList, true) {

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

            Glide.with(context).load(appIcon).into(itemView.appIconIV)
            itemView.appTitleTV.text = appName

            itemView.currentLimitTV.text =
                "Current limit: ${TextUtils.getAppLimitText(appLimit.currentLimit)}"

            itemView.setOnClickListener {
                displayEditOrDeleteDialog(itemView.context, appLimit)
            }
        }

        private fun displayEditOrDeleteDialog(context: Context, appLimit: AppLimit) {
            val databaseRepository = (context.applicationContext as UsageSafeApp).component.getDatabaseRepository()
            val dialog = AppLimitEditDialog(context, appLimit, databaseRepository)
            dialog.show()
        }
    }
}