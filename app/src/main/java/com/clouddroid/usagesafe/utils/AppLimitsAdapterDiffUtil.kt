package com.clouddroid.usagesafe.utils

import androidx.recyclerview.widget.DiffUtil
import com.clouddroid.usagesafe.models.AppDetails

class AppLimitsAdapterDiffUtil(private val oldList: List<AppDetails>, private val newList: List<AppDetails>) :
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
        return oldList[oldItemPosition].name == newList[newItemPosition].name
                && oldList[oldItemPosition].icon?.equals(newList[newItemPosition]) ?: false
                && oldList[oldItemPosition].isSystemApp == newList[newItemPosition].isSystemApp
    }
}