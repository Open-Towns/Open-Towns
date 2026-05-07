package xaos.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//new Date() starts at day 1, month 1, year 1.

class DateTest {

    @Test
    void newDateStartsAtDayOneMonthOneYearOne() {
        Date date = new Date();
        // Check that a new Date starts at day 1, month 1, year 1.
        assertEquals(1, date.getDay());
        assertEquals(1, date.getMonth());
        assertEquals(1, date.getYear());
        // Also check the string version of the date.
        assertEquals("1 / 1 / 1", date.toString());
    }

    @Test
    void addDayIncrementsDay() {
        Date date = new Date();
        // Move the date forward by one day.
        date.addDay();
        // The day should increase from 1 to 2.
        // The month and year should stay the same.
        assertEquals(2, date.getDay());
        assertEquals(1, date.getMonth());
        assertEquals(1, date.getYear());
    }

    @Test
    void addDayRollsOverToNextMonthAfterThirtyDays() {
        Date date = new Date();
        // Set the date to the final day of a month.
        date.setDay(30);
        date.setMonth(1);
        date.setYear(1);
        // Move forward one day.
        date.addDay();
        // After day 30, the date should roll over to day 1 of the next month.
        // Year should not change yet.
        assertEquals(1, date.getDay());
        assertEquals(2, date.getMonth());
        assertEquals(1, date.getYear());
    }

    @Test
    void addDayRollsOverToNextYearAfterMonthTwelve() {
        Date date = new Date();
        // Set the date to the final day of the final month of the year.
        date.setDay(30);
        date.setMonth(12);
        date.setYear(1);
        // Move forward one day.
        date.addDay();
        // After day 30 of month 12, the date should become:
        // day 1, month 1, year 2.
        assertEquals(1, date.getDay());
        assertEquals(1, date.getMonth());
        assertEquals(2, date.getYear());
    }
}