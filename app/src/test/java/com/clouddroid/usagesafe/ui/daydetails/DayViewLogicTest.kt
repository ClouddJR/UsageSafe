package com.clouddroid.usagesafe.ui.daydetails

import com.clouddroid.usagesafe.util.DayBegin
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import junitparams.naming.TestCaseName
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(JUnitParamsRunner::class)
class DayViewLogicTest {

    // May 1, 2019- setting this date as today
    private val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2019)
        set(Calendar.MONTH, 4)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    // April 28, 2019- setting this date as a date with the earliest usage log saved in database
    private val firstSavedLogCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2019)
        set(Calendar.MONTH, 3)
        set(Calendar.DAY_OF_MONTH, 28)
    }

    @Test
    fun `should correctly initialize current day after creating an instance`() {
        //given this today calendar

        //when creating an instance of a DayViewLogic
        val dayViewLogic = DayViewLogic(calendar, DayBegin._12AM.toInt(), firstSavedLogCalendar)

        //should update its field with calendar passed through the constructor
        assertEquals(dayViewLogic.currentDay, calendar)
    }

    @Test
    fun `should correctly signal that the current day is the earliest one`() {
        //given this dayViewLogic instance
        val dayViewLogic = DayViewLogic(calendar, DayBegin._12AM.toInt(), firstSavedLogCalendar)

        //when setting current day as the same day as the one with the earliest log
        dayViewLogic.setPreviousDayAsCurrent()
        dayViewLogic.setPreviousDayAsCurrent()
        dayViewLogic.setPreviousDayAsCurrent()

        //should correctly indicate that the current day is the earliest
        assertTrue(dayViewLogic.isCurrentDayTheEarliest)
    }

    @Test
    fun `should correctly signal that the current day is the latest one`() {
        //given this dayViewLogic instance
        val dayViewLogic = DayViewLogic(calendar, DayBegin._12AM.toInt(), firstSavedLogCalendar)

        //should correctly indicate that the current day is the latest
        assertTrue(dayViewLogic.isCurrentDayTheLatest)
    }

    @Test
    fun `should correctly calculate previous day`() {
        //given this dayViewLogic instance
        val dayViewLogic = DayViewLogic(calendar, DayBegin._12AM.toInt(), firstSavedLogCalendar)

        //when setting previous day as current
        dayViewLogic.setPreviousDayAsCurrent()

        //should correctly update the inner variable
        assertEquals(dayViewLogic.currentDay.get(Calendar.DAY_OF_MONTH), 30)
        assertEquals(dayViewLogic.currentDay.get(Calendar.MONTH), 3)
    }

    @Test
    fun `should correctly calculate next day`() {
        //given this dayViewLogic instance
        val dayViewLogic = DayViewLogic(calendar, DayBegin._12AM.toInt(), firstSavedLogCalendar)

        //when setting next day as current
        dayViewLogic.setNextDayAsCurrent()

        //should correctly update the inner variable
        assertEquals(dayViewLogic.currentDay.get(Calendar.DAY_OF_MONTH), 2)
        assertEquals(dayViewLogic.currentDay.get(Calendar.MONTH), 4)
    }

    @Test
    @Parameters(
        DayBegin._12AM,
        DayBegin._1AM,
        DayBegin._2AM,
        DayBegin._3AM,
        DayBegin._4AM,
        DayBegin._5AM,
        DayBegin._6AM,
        DayBegin._7AM,
        DayBegin._8AM,
        DayBegin._9AM,
        DayBegin._10AM
    )
    @TestCaseName("{method} (hour: {index})")
    fun `should get correct day range for different day begin preferences`(dayBegin: String) {
        //given day begin preference
        val dayViewLogic = DayViewLogic(calendar, dayBegin.toInt(), firstSavedLogCalendar)

        //when calling getDayRange()
        val pair = dayViewLogic.getDayRange()

        //should return calendars with proper hours
        assertEquals(
            dayBegin.toInt(),
            Calendar.getInstance().apply { timeInMillis = pair.first }.get(Calendar.HOUR_OF_DAY)
        )
        assertEquals(0, Calendar.getInstance().apply { timeInMillis = pair.first }.get(Calendar.MINUTE))
        assertEquals(0, Calendar.getInstance().apply { timeInMillis = pair.first }.get(Calendar.SECOND))
        assertEquals(0, Calendar.getInstance().apply { timeInMillis = pair.first }.get(Calendar.MILLISECOND))

        assertTrue(
            Calendar.getInstance().apply { timeInMillis = pair.second } ==
                    Calendar.getInstance().apply {
                        timeInMillis = pair.first
                        add(Calendar.HOUR_OF_DAY, 23)
                        add(Calendar.MINUTE, 59)
                    }
        )

    }

}