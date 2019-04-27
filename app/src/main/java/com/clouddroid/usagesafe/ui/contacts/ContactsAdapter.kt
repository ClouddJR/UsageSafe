package com.clouddroid.usagesafe.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.data.model.Contact
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactsAdapter(private var contactsList: List<Contact>) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contactsList[position])
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    fun setData(list: List<Contact>) {
        contactsList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(contact: Contact) {
            itemView.nameTV.text = contact.name
            itemView.emailTV.text = contact.email
        }
    }
}