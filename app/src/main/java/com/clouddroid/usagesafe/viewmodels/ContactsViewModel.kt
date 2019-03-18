package com.clouddroid.usagesafe.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clouddroid.usagesafe.models.Contact
import com.clouddroid.usagesafe.repositories.DatabaseRepository
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