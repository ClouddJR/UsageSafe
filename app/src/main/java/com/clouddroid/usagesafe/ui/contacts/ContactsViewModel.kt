package com.clouddroid.usagesafe.ui.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.data.model.Contact
import com.clouddroid.usagesafe.data.local.DatabaseRepository
import javax.inject.Inject

class ContactsViewModel @Inject constructor(private val databaseRepository: DatabaseRepository) : ViewModel() {

    val contactsList = MutableLiveData<List<Contact>>()

    fun init() {
        contactsList.value = databaseRepository.getListOfContacts()
    }

    fun addNewContact(name: String, email: String) {
        val contact = Contact()
        contact.name = name
        contact.email = email
        databaseRepository.addContact(contact)
    }
}