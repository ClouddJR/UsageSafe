package com.clouddroid.usagesafe.ui.todaystats

import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppUsageInfo
import com.clouddroid.usagesafe.ui.appdetails.AppDetailsActivity
import com.clouddroid.usagesafe.util.PackageInfoUtils
import com.clouddroid.usagesafe.util.TextUtils
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.ad_most_used_adapter.view.*
import kotlinx.android.synthetic.main.item_app_usage_details.view.*
import kotlinx.android.synthetic.main.item_app_usage_details.view.adHeadline
import kotlinx.android.synthetic.main.item_app_usage_details.view.adIcon


class MostUsedAdapter(private val items: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itemType = 0
    private val adType = 1

    fun addItems(appsList: List<AppUsageInfo>) {
        items.addAll(appsList)
        notifyItemRangeInserted(6, appsList.size)
    }

    fun addAd(nativeAd: UnifiedNativeAd) {
        items.add(1, nativeAd)
        notifyItemInserted(1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == adType) {
            AdViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.ad_most_used_adapter,
                    parent,
                    false
                )
            )
        } else {
            ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_app_usage_details,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> holder.bind(items[position] as AppUsageInfo)
            is AdViewHolder -> holder.bind(items[position] as UnifiedNativeAd)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val obj = items[position]
        return if (obj is UnifiedNativeAd) {
            adType
        } else {
            itemType
        }
    }

    override fun getItemCount() = items.size

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

            itemView.adIcon.setImageDrawable(
                PackageInfoUtils.getRawAppIcon(
                    appUsageInfo.packageName,
                    itemView.context
                )
            )

            itemView.adHeadline.text = PackageInfoUtils.getAppName(appUsageInfo.packageName, itemView.context)
        }

    }

    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var adView: UnifiedNativeAdView = itemView.findViewById(R.id.adView)

        init {
            adView.headlineView = adView.findViewById(R.id.adHeadline)
            adView.callToActionView = adView.findViewById(R.id.adCallToAction)
            adView.iconView = adView.findViewById(R.id.adIcon)
            adView.priceView = adView.findViewById(R.id.adPrice)
            adView.starRatingView = adView.findViewById(R.id.adStars)
            adView.storeView = adView.findViewById(R.id.adStore)
            adView.advertiserView = adView.findViewById(R.id.adAdvertiser)
        }

        fun bind(nativeAd: UnifiedNativeAd) {
            populateNativeAdView(nativeAd)
        }

        private fun populateNativeAdView(nativeAd: UnifiedNativeAd) {
            // These assets are guaranteed to be in every UnifiedNativeAd
            (adView.headlineView as TextView).text = nativeAd.headline
            (adView.callToActionView as TextView).text = nativeAd.callToAction

            // These assets aren't guaranteed to be in every UnifiedNativeAd
            val icon = nativeAd.icon

            if (icon == null) {
                adView.iconView.visibility = View.GONE
                val params = itemView.adAdvertiser.layoutParams as ConstraintLayout.LayoutParams
                params.startToStart = itemView.adHeadline.id
                params.marginStart = 0
                itemView.adAdvertiser.layoutParams = params
            } else {
                (adView.iconView as ImageView).setImageDrawable(icon.drawable)
                adView.iconView.visibility = View.VISIBLE
                val params = itemView.adAdvertiser.layoutParams as ConstraintLayout.LayoutParams
                params.startToEnd = itemView.adIcon.id
                params.marginStart = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24f, itemView.context.resources
                        .displayMetrics
                ).toInt()
                itemView.adAdvertiser.layoutParams = params
            }

            if (nativeAd.price == null) {
                adView.priceView.visibility = View.INVISIBLE
            } else {
                adView.priceView.visibility = View.VISIBLE
                (adView.priceView as TextView).text = nativeAd.price
            }

            if (nativeAd.store == null) {
                adView.storeView.visibility = View.INVISIBLE
            } else {
                adView.storeView.visibility = View.VISIBLE
                (adView.storeView as TextView).text = nativeAd.store
            }

            if (nativeAd.starRating == null) {
                adView.starRatingView.visibility = View.INVISIBLE
            } else {
                (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
                adView.starRatingView.visibility = View.VISIBLE
            }

            if (nativeAd.advertiser == null) {
                adView.advertiserView.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView.visibility = View.VISIBLE
            }

            adView.setNativeAd(nativeAd)
        }
    }
}