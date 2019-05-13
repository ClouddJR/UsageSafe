package com.clouddroid.usagesafe.ui.common

import com.clouddroid.usagesafe.util.WeekBegin
import junit.framework.Assert.*
import org.junit.Test
import java.util.*

class WeekViewLogicTest {

    // May 1, 2019- setting this date as today
    private val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2019)
        set(Calendar.MONTH, 4)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    // April 14, 2019- setting this date as a date with the earliest usage log saved in database
    private val firstSavedLogCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2019)
        set(Calendar.MONTH, 3)
        set(Calendar.DAY_OF_MONTH, 14)
    }


    @Test
    fun `should correctly initialize current week after creating an instance`() {
        // testing when week begin is Monday
        var weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.MONDAY)

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 29)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)

        // testing when week begin is Sunday
        weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.SUNDAY)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 28)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)

        // testing when week begin is six days ago
        weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.SIX_DAYS_AGO)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 25)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)
    }


    @Test
    fun `should signal that current week is the last one when it contains today's date`() {
        val weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.MONDAY)
        assertTrue(weekViewLogic.isCurrentWeekTheLatest)

        // go back one week to check if now the variable is correctly set to false
        weekViewLogic.setPreviousWeekAsCurrent()
        assertFalse(weekViewLogic.isCurrentWeekTheLatest)
    }

    @Test
    fun `should signal that current week is the earliest one when it contains first saved log date`() {
        val weekViewLogic = WeekViewLogic(firstSavedLogCalendar, firstSavedLogCalendar, WeekBegin.MONDAY)
        assertTrue(weekViewLogic.isCurrentWeekTheEarliest)

        // go one week further to check if now the variable is correctly set to false
        weekViewLogic.setNextWeekAsCurrent()
        assertFalse(weekViewLogic.isCurrentWeekTheEarliest)
    }

    @Test
    fun `should correctly update current week when week begin preference is changed`() {
        val weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.MONDAY)

        // change week begin to Sunday
        weekViewLogic.weekBegin = WeekBegin.SUNDAY
        weekViewLogic.refreshWeek()
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 28)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)

        // change week begin to Monday
        weekViewLogic.weekBegin = WeekBegin.MONDAY
        weekViewLogic.refreshWeek()
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 29)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)

        // change week begin to six days ago
        weekViewLogic.weekBegin = WeekBegin.SIX_DAYS_AGO
        weekViewLogic.refreshWeek()
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 25)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)
    }

    @Test
    fun `should correctly calculate previous week`() {
        // when week begin is Monday
        var weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.MONDAY)
        weekViewLogic.setPreviousWeekAsCurrent()

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 22)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)

        // when week begin is Sunday
        weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.SUNDAY)
        weekViewLogic.setPreviousWeekAsCurrent()

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 21)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)

        // when week begin is six days ago
        weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.SIX_DAYS_AGO)
        weekViewLogic.setPreviousWeekAsCurrent()

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 18)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 3)
    }

    @Test
    fun `should correctly calculate next week`() {
        // when week begin is Monday
        var weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.MONDAY)
        weekViewLogic.setNextWeekAsCurrent()

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 6)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 4)

        // when week begin is Sunday
        weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.SUNDAY)
        weekViewLogic.setNextWeekAsCurrent()

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 5)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 4)

        // when week begin is six days ago
        weekViewLogic = WeekViewLogic(calendar, firstSavedLogCalendar, WeekBegin.SIX_DAYS_AGO)
        weekViewLogic.setNextWeekAsCurrent()

        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.DAY_OF_MONTH), 2)
        assertEquals(weekViewLogic.currentWeek.first.get(Calendar.MONTH), 4)

    }
}