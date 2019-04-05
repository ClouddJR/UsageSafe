package com.clouddroid.usagesafe.repositories

import android.content.Context
import android.os.HandlerThread
import com.clouddroid.usagesafe.models.AppLimit
import com.clouddroid.usagesafe.models.Contact
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.models.ScreenLimit
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*

class DatabaseRepository {

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

    fun addLogEvent(logEvents: List<LogEvent>, listener: () -> Unit) {
        val chunkedList = logEvents.chunked(500)
        chunkedList.forEach { list ->
            val realm = Realm.getInstance(config)
            if (chunkedList.last() == list) {
                realm.executeTransactionAsync({
                    it.insertOrUpdate(list)
                }, {
                    listener.invoke()
                }, {})
            } else {
                realm.executeTransactionAsync {
                    it.insertOrUpdate(list)
                }
            }

        }
    }

    fun getLogEventsFromRange(beginMillis: Long, endMillis: Long): Observable<List<LogEvent>> {
        return Observable.create { emitter ->
            while (!initialSetupFinished) {
            }
            val realm = Realm.getInstance(config)
            emitter.onNext(
                realm.copyFromRealm(
                    realm.where(LogEvent::class.java).between("timestamp", beginMillis, endMillis).findAll()
                )
            )
            emitter.onComplete()
            realm.close()
        }
    }

    fun getNumberOfLogs(beginMillis: Long, endMillis: Long): Int {
        val realm = Realm.getInstance(config)
        val numberOfLogs =
            realm.where(LogEvent::class.java).between("timestamp", beginMillis, endMillis).findAll().count()
        realm.close()
        return numberOfLogs
    }

    fun getListOfContacts(): List<Contact> {
        val realm = Realm.getInstance(config)
        val listOfContacts = realm.where(Contact::class.java).findAll()
        realm.close()
        return listOfContacts
    }

    fun getFirstLogEvent(): Calendar {
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

    fun realmScheduler(): Scheduler {
        val t = HandlerThread("realmThread")
        if (!t.isAlive)
            t.start()
        return AndroidSchedulers.from(t.looper)
    }


}