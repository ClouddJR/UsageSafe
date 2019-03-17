package com.clouddroid.usagesafe.models

import io.realm.RealmObject

open class ScreenLimit : RealmObject() {
    var limitMillis: Long = 0
}