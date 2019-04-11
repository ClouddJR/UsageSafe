package com.clouddroid.usagesafe.ui.contacts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.clouddroid.usagesafe.R
import com.clouddroid.usagesafe.ui.base.BaseFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_contacts_list.*

class ContactsListFragment : BaseFragment() {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var adapter: ContactsAdapter

    override fun getLayoutId() = R.layout.fragment_contacts_list

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV()
        initOnFABClick()
        observeDataChanges()
    }

    fun scrollToTop() {
        nestedScroll.smoothScrollTo(0, 0)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[ContactsViewModel::class.java]
        viewModel.init()
    }

    private fun initRV() {
        adapter = ContactsAdapter(mutableListOf())
        contactsListRV.adapter = adapter
    }

    private fun initOnFABClick() {
        addContactFAB.setOnClickListener {
            displayAddContactDialog()
        }
    }

    private fun displayAddContactDialog() {
        val dialog = AlertDialog.Builder(context!!, R.style.AlertDialog)
            .setView(R.layout.dialog_add_contact)
            .show()

        val nameEditText = dialog.findViewById<EditText>(R.id.nameET)
        val emailEditText = dialog.findViewById<EditText>(R.id.emailET)

        dialog.findViewById<MaterialButton>(R.id.addContactBT)?.setOnClickListener {
            viewModel.addNewContact(nameEditText?.text.toString(), emailEditText?.text.toString())
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun observeDataChanges() {
        viewModel.contactsList.observe(this, Observer {
            adapter.setData(it)
        })
    }

}