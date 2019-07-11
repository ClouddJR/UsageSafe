package com.clouddroid.usagesafe.data.repository

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.clouddroid.usagesafe.data.local.LocalDatabase
import com.clouddroid.usagesafe.data.model.AppLimit
import com.clouddroid.usagesafe.data.model.LogEvent
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DatabaseRepositoryTest {

    private val synchronousDelegate = object : TaskExecutor() {
        override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
        override fun postToMainThread(runnable: Runnable) = runnable.run()
        override fun isMainThread() = true
    }

    private lateinit var database: LocalDatabase
    private lateinit var databaseRepository: DatabaseRepository

    @Before
    fun setUp() {
        ArchTaskExecutor.getInstance().setDelegate(synchronousDelegate)
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            LocalDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        databaseRepository =
            DatabaseRepository(
                database.appLimitDao(),
                database.logEventDao(),
                database.groupLimitDao(),
                database.focusModeAppDao()
            )
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        database.close()
    }

    @Test
    fun shouldCorrectlyAddAppLimit() {
        // given this app limit
        val appLimit = AppLimit()
        appLimit.packageName = "com.test.package"
        appLimit.limit = 1

        // when saving and retrieving all limits from db
        databaseRepository.addAppLimit(appLimit)

        // then it should give the same app limit back
        val testSubscriber = databaseRepository.getListOfLimits().test()

        testSubscriber.assertValue { it.first() == appLimit }
    }

    @Test
    fun shouldCorrectlyDeleteAppLimit() {
        // given this app limit
        val appLimit = AppLimit()
        appLimit.packageName = "com.test.package"
        appLimit.limit = 1

        // when saving and then deleting this app limit
        databaseRepository.addAppLimit(appLimit)
        databaseRepository.deleteAppLimit(appLimit)

        // then it should return empty list
        databaseRepository.getListOfLimits().test().assertValue { it.isEmpty() }
    }

    @Test
    fun shouldCorrectlySaveLogEvents() {
        // given list of log events
        val logsList = listOf(
            LogEvent().apply {
                packageName = "com.test.package"
                type = 0
                timestamp = 1000
            },
            LogEvent().apply {
                packageName = "com.test.package2"
                type = 1
                timestamp = 2000
            }
        )

        // when saving them to db
        databaseRepository.addLogEvents(logsList)

        // then it should give the same list back
        val list = databaseRepository.getLogEventsFromRange(1000, 2000)
        assertEquals(list.size, logsList.size)
        assertEquals(list.any { it.packageName == logsList[0].packageName }, true)
        assertEquals(list.any { it.packageName == logsList[1].packageName }, true)
    }

    @Test
    fun shouldCorrectlyGetLogsFromCertainRange() {
        // given list of log events
        val logsList = listOf(
            LogEvent().apply {
                packageName = "com.test.package1"
                type = 0
                timestamp = 998L
            },
            LogEvent().apply {
                packageName = "com.test.package2"
                type = 0
                timestamp = 999L
            },
            LogEvent().apply {
                packageName = "com.test.package3"
                type = 1
                timestamp = 2000L
            },
            LogEvent().apply {
                packageName = "com.test.package4"
                type = 1
                timestamp = 2001L
            },
            LogEvent().apply {
                packageName = "com.test.package5"
                type = 1
                timestamp = 2002L
            }
        )

        // when saving them to db
        databaseRepository.addLogEvents(logsList)

        // then it should return correct logs for different ranges
        var list = databaseRepository.getLogEventsFromRange(1000, 2000)
        assertEquals(list.size, 1)

        list = databaseRepository.getLogEventsFromRange(998, 2001)
        assertEquals(list.size, 4)

        list = databaseRepository.getLogEventsFromRange(2003, 2005)
        assertEquals(list.size, 0)
    }

    @Test
    fun shouldCorrectlyGetTheEarliestLogEventWhenDbIsNotEmpty() {
        // given list of log events
        val logsList = listOf(
            LogEvent().apply {
                packageName = "com.test.package1"
                type = 0
                timestamp = 998L
            },
            LogEvent().apply {
                packageName = "com.test.package2"
                type = 0
                timestamp = 999L
            },
            LogEvent().apply {
                packageName = "com.test.package3"
                type = 1
                timestamp = 2000L
            },
            LogEvent().apply {
                packageName = "com.test.package4"
                type = 1
                timestamp = 2001L
            },
            LogEvent().apply {
                packageName = "com.test.package5"
                type = 1
                timestamp = 2002L
            }
        )

        // when saving them to db
        databaseRepository.addLogEvents(logsList)

        //then it should give correct earliest log
        assertEquals(databaseRepository.getTheEarliestLogEvent().timeInMillis, 998)

    }

    @Test
    fun shouldCorrectlyGetTheEarliestLogEventWhenDbIsEmpty() {
        // given empty log events table

        // when getting the earliest log event

        //then it should give calendar from six days ago
        assertEquals(
            databaseRepository.getTheEarliestLogEvent().get(Calendar.DAY_OF_MONTH),
            Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -6) }.get(Calendar.DAY_OF_MONTH)
        )
    }

}