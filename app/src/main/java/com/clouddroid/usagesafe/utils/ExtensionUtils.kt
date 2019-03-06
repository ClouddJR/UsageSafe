package com.clouddroid.usagesafe.utils

import androidx.fragment.app.FragmentManager
import com.clouddroid.usagesafe.fragments.BaseFragment

object ExtensionUtils {

    fun FragmentManager.replaceWithTransaction(containerId: Int, fragment: BaseFragment) {
        this.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }
}