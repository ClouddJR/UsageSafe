package com.clouddroid.usagesafe.util

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseFragment
import java.util.*

object ExtensionUtils {

    fun FragmentManager.doesNotContain(fragment: BaseFragment): Boolean {
        return !this.fragments.contains(fragment)
    }

    fun FragmentManager.addAndCommit(fragment: BaseFragment, tag: String) {
        this.beginTransaction()
            .add(R.id.fragmentPlaceHolder, fragment, tag)
            .hide(fragment)
            .commit()
    }

    fun FragmentTransaction.showAndHideOthers(fragment: BaseFragment, fragments: List<Fragment>) {
        this.show(fragment)
        fragments.forEach {
            if (it != fragment) this.hide(it)
        }
    }

    fun Calendar.isBefore(other: Calendar): Boolean {
        return (this.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.before(other)
    }

    fun Calendar.isTheSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR)
                && this.get(Calendar.MONTH) == other.get(Calendar.MONTH)
                && this.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
    }

    fun Calendar.isWithin(start: Calendar, end: Calendar): Boolean {
        return this.after(start) && this.before(end)
    }

    fun View.show() {
        this.visibility = View.VISIBLE
    }

    fun View.hide() {
        this.visibility = View.INVISIBLE
    }
}