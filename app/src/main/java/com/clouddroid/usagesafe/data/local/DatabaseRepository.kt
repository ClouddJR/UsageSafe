package com.clouddroid.usagesafe.data.local

import android.content.Context
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.Contact
import com.clouddroid.usagesafe.data.model.LogEvent
import com.clouddroid.usagesafe.data.model.ScreenLimit
import io.reactivex.Single
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*

class DatabaseRepository {

    //this variable is used at every app launch so that the data is first saved
    //to the database before being accessed by history-related fragments
    var initialSetupFinished = false

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

    fun addAppLimit(appLimit: AppLimit) {
        val realm = Realm.getInstance(config)
        realm.executeTransactionAsync {
            it.insertOrUpdate(appLimit)
        }
    }

    fun saveScreenLimit(screenLimit: ScreenLimit) {
        val realm = Realm.getInstance(config)
        realm.executeTransactionAsync {
            it.insertOrUpdate(screenLimit)
        }
    }

    fun addContact(contact: Contact) {
        val realm = Realm.getInstance(config)
        realm.executeTransactionAsync {
            it.insertOrUpdate(contact)
        }
    }

    fun addLogEvent(logEvents: List<LogEvent>, onFinishedListener: () -> Unit) {
        val chunkedList = logEvents.chunked(500)
        chunkedList.forEach { list ->
            val realm = Realm.getInstance(config)
            if (chunkedList.last() == list) {
                realm.executeTransactionAsync({
                    it.insertOrUpdate(list)
                }, {
                    onFinishedListener.invoke()
                }, {})
            } else {
                realm.executeTransactionAsync {
                    it.insertOrUpdate(list)
                }
            }

        }
    }

    fun getLogEventsFromRange(beginMillis: Long, endMillis: Long): Single<List<LogEvent>> {
        return Single.create { emitter ->

            //waiting for the data to be saved in database before accessing it
            while (!initialSetupFinished) {
            }

            val realm = Realm.getInstance(config)
            emitter.onSuccess(
                realm.copyFromRealm(
                    realm.where(LogEvent::class.java).between("timestamp", beginMillis, endMillis).findAll()
                )
            )
            realm.close()
        }
    }

    fun getTheEarliestLogEvent(): Calendar {
        val realm = Realm.getInstance(config)
        val firstLogCalendar = Calendar.getInstance().apply {
            timeInMillis = (realm.where(LogEvent::class.java)
                .equalTo(
                    "timestamp", realm.where(LogEvent::class.java).min("timestamp")?.toLong() ?: timeInMillis
                ).findFirst()?.timestamp) ?: timeInMillis
        }
        realm.close()
        return firstLogCalendar
    }

    fun removeLogsBetweenRange(beginMillis: Long, endMillis: Long) {
        val realm = Realm.getInstance(config)
        realm.executeTransaction {
            it.where(LogEvent::class.java).between("timestamp", beginMillis, endMillis)
                .findAll().deleteAllFromRealm()
        }
    }

    fun getListOfContacts(): List<Contact> {
        val realm = Realm.getInstance(config)
        val listOfContacts = realm.where(Contact::class.java).findAll()
        realm.close()
        return listOfContacts
    }

    fun getListOfLimits(): List<AppLimit> {
        val realm = Realm.getInstance(config)
        val listOfAppLimits = realm.where(AppLimit::class.java).findAll()
        realm.close()
        return listOfAppLimits
    }
}