package com.clouddroid.usagesafe.repositories

import android.content.Context
import com.clouddroid.usagesafe.models.AppLimit
import com.clouddroid.usagesafe.models.Contact
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.models.ScreenLimit
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*

class DatabaseRepository {

    private var realm: Realm
    private val config = RealmConfiguration
        .Builder()
        .deleteRealmIfMigrationNeeded()
        .compactOnLaunch()
        .build()

    object RealmInitializer {
        fun initRealm(context: Context) {
            Realm.init(context)
        }
    }

    init {
        realm = Realm.getInstance(config)
    }

    fun addAppLimit(appLimit: AppLimit) {
        realm.executeTransactionAsync {
            it.insertOrUpdate(appLimit)
        }
    }

    fun saveScreenLimit(screenLimit: ScreenLimit) {
        realm.executeTransactionAsync {
            it.insertOrUpdate(screenLimit)
        }
    }

    fun addContact(contact: Contact) {
        realm.executeTransactionAsync {
            it.insertOrUpdate(contact)
        }
    }

    fun addLogEvent(logEvents: List<LogEvent>) {
        logEvents.chunked(500).forEach { list ->
            realm.executeTransactionAsync {
                it.insertOrUpdate(list)
            }
        }
    }

    fun getLogEventsFromRange(beginMillis: Long, endMillis: Long): List<LogEvent> {
        val realm = Realm.getInstance(config)
        val logsList = realm.copyFromRealm(
            realm.where(LogEvent::class.java).between("timestamp", beginMillis, endMillis).findAll()
        )
        realm.close()
        return logsList
    }

    fun getListOfContacts(): List<Contact> {
        return realm.where(Contact::class.java).findAll()
    }

    fun getFirstLogEvent(): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = (realm.where(LogEvent::class.java)
                .equalTo(
                    "timestamp", realm.where(LogEvent::class.java).min("timestamp")?.toLong() ?: 0
                ).findFirst()?.timestamp) ?: timeInMillis
        }
    }
}