package com.clouddroid.usagesafe.ui.historystats

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.historystats.applaunches.AppLaunchesFragment
import com.clouddroid.usagesafe.ui.historystats.screen.ScreenTimeFragment
import com.clouddroid.usagesafe.ui.historystats.unlocks.UnlocksFragment

class ViewPagerAdapter(private val context: Context, manager: FragmentManager) :
    FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) = when (position) {
        0 -> ScreenTimeFragment()
        1 -> UnlocksFragment()
        2 -> AppLaunchesFragment()
        else -> throw IllegalStateException("Unexpected position $position")
    }

    override fun getCount(): Int = 3

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> context.getString(R.string.fragment_history_screen_time)
        1 -> context.getString(R.string.fragment_history_unlocks)
        2 -> context.getString(R.string.fragment_history_app_launches)
        else -> throw IllegalStateException("Unexpected position $position")
    }
}