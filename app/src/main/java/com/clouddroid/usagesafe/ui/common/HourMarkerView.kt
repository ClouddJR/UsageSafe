package com.clouddroid.usagesafe.ui.common

import android.content.Context

import android.widget.TextView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.daydetails.DayDetailsViewModel
import com.clouddroid.usagesafe.util.TextUtils
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

class HourMarkerView(context: Context, layoutId: Int, private val mode: Int, private val hoursNames: List<String>) :
    MarkerView(context, layoutId) {

    private var titleTextView: TextView = findViewById(R.id.valueTV)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        val marketText = if (mode == DayDetailsViewModel.MODE.SCREEN_TIME) {
            "${hoursNames[e?.x?.toInt() ?: 0]} : ${TextUtils.getTotalScreenTimeText(
                e?.y?.toLong() ?: 0L, context
            )}"
        } else {
            "${hoursNames[e?.x?.toInt() ?: 0]} : ${"%.0f".format(e?.y)}"
        }

        titleTextView.text = marketText
        super.refreshContent(e, highlight)
    }
}