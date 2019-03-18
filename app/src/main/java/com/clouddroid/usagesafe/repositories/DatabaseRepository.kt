package com.clouddroid.usagesafe.repositories

import android.content.Context
import com.clouddroid.usagesafe.models.AppLimit
import com.clouddroid.usagesafe.models.LogEvent
import com.clouddroid.usagesafe.models.ScreenLimit
import io.realm.Realm
import io.realm.RealmConfiguration

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
            it.copyToRealmOrUpdate(appLimit)
        }
    }

    fun saveScreenLimit(screenLimit: ScreenLimit) {
        realm.executeTransactionAsync {
            it.insertOrUpdate(screenLimit)
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
        val list = mutableListOf<LogEvent>()
        realm.executeTransactionAsync {
            list.addAll(it.where(LogEvent::class.java).between("timestamp", beginMillis, endMillis).findAll())
        }
        return list
    }
}