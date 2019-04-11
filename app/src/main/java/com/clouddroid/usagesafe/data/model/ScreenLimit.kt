package com.clouddroid.usagesafe.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ScreenLimit : RealmObject() {

    @PrimaryKey
    var id: Int = 1 //only one object
    var limitMillis: Long = 0
}