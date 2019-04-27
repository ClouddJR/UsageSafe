package com.clouddroid.usagesafe.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class AppLimit : RealmObject() {

    @PrimaryKey
    var packageName: String = ""
    var currentLimit: Long = 0
    var lastNotification: Date = Date(0)
}