package com.clouddroid.usagesafe.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.fragments.BaseFragment

object ExtensionUtils {

//****************Fragment Manager**********************************************************************

    fun FragmentManager.doesNotContain(fragment: BaseFragment): Boolean {
        return !this.fragments.contains(fragment)
    }

    fun FragmentManager.addAndCommit(fragment: BaseFragment) {
        this.beginTransaction()
            .add(R.id.fragmentPlaceHolder, fragment)
            .commit()
    }

    fun FragmentTransaction.showAndHideOthers(fragment: BaseFragment, fragments: List<Fragment>) {
        this.show(fragment)
        fragments.forEach {
            if (it != fragment) this.hide(it)
        }
    }

//****************Fragment Manager**********************************************************************

}