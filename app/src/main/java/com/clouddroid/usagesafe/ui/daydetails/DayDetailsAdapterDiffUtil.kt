package com.clouddroid.usagesafe.ui.daydetails

import androidx.recyclerview.widget.DiffUtil
import com.clouddroid.usagesafe.data.model.AppUsageInfo

class DayDetailsAdapterDiffUtil(private val oldList: List<AppUsageInfo>, private val newList: List<AppUsageInfo>) :
    DiffUtil.Callback() {

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