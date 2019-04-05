package com.clouddroid.usagesafe.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.clouddroid.usagesafe.R

class SpinnerAdapter<T>(context: Context, themeId: Int, private val objects: Array<T>) :
    ArrayAdapter<T>(context, themeId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.spinner_selected_item, parent, false)
        view.findViewById<TextView>(R.id.itemTV).text = objects[position].toString()
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.spinner_list_item, parent, false)
        view.findViewById<TextView>(R.id.itemTV).text = objects[position].toString()
        return view
    }
}