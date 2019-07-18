package com.clouddroid.usagesafe.ui.applimits.adddialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.AppDetails
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_app_info.view.*

class AppListAdapter(private val context: Context, val onItemClicked: (String) -> Unit) :
    RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    private lateinit var disposable: Disposable

    private val appsList = mutableListOf<AppDetails>()

    fun swapItems(appDetailsList: List<AppDetails>) {
        //dispose previous calculation if exist
        if (::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }

        disposable = Single.fromCallable {
            DiffUtil.calculateDiff(AppListAdapterDiffUtil(appsList, appDetailsList))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { diffResult ->
                appsList.clear()
                appsList.addAll(appDetailsList)
                diffResult.dispatchUpdatesTo(this)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app_info, parent, false))

    override fun getItemCount() = appsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(appsList[position])


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(appDetails: AppDetails) {
            Glide.with(context).load(appDetails.icon).into(itemView.adIcon)
            itemView.appTitle.text = appDetails.name

            itemView.setOnClickListener {
                onItemClicked.invoke(appDetails.packageName)
            }
        }
    }
}