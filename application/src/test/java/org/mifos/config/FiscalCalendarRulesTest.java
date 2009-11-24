/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.util.LocalizationConverter;
import org.mifos.framework.util.helpers.FilePaths;
import org.testng.annotations.Test;

@Test(groups={"unit", "configTestSuite"})
public class FiscalCalendarRulesTest extends TestCase {

    public FiscalCalendarRulesTest() {
        super();
        init();
    }

    public FiscalCalendarRulesTest(String name) {
        super(name);
        init();
    }

    private static ConfigurationManager configMgr = null;
    private static String savedConfigWorkingDays = null;

    public static void init() {
        MifosLogManager.configureLogging();
        configMgr = ConfigurationManager.getInstance();
        savedConfigWorkingDays = configMgr.getProperty(FiscalCalendarRules.FiscalCalendarRulesWorkingDays).toString();
        savedConfigWorkingDays = savedConfigWorkingDays.replace("[", "");
        savedConfigWorkingDays = savedConfigWorkingDays.replace("]", "");

    }

    private void setSavedConfig() {
        configMgr.setProperty(FiscalCalendarRules.FiscalCalendarRulesWorkingDays, savedConfigWorkingDays);
        FiscalCalendarRules.reloadConfigWorkingDays();
    }

    private void setNewWorkingDays(String newWorkingDays) {
        configMgr.setProperty(FiscalCalendarRules.FiscalCalendarRulesWorkingDays, newWorkingDays);
        FiscalCalendarRules.reloadConfigWorkingDays();
    }

    public void testGetWorkingDays() {

        String configWorkingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY";
        setNewWorkingDays(configWorkingDays);
        List<WeekDay> workingDays = FiscalCalendarRules.getWorkingDays();
       Assert.assertEquals(workingDays.size(), 6);
        WeekDay[] weekDays = WeekDay.values();
        for (int i = 0; i < workingDays.size(); i++)
           Assert.assertEquals(workingDays.get(i).toString(), weekDays[i + 1].name());
        configWorkingDays = "TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        setNewWorkingDays(configWorkingDays);
        workingDays = FiscalCalendarRules.getWorkingDays();
       Assert.assertEquals(workingDays.size(), 4);
        for (int i = 0; i < workingDays.size(); i++)
           Assert.assertEquals(workingDays.get(i).toString().toUpperCase(), weekDays[i + 2].name().toUpperCase());
        // set it back
        setSavedConfig();

    }

    public void testGetWeekDaysList() {
        List<WeekDay> weekDaysFromFiscalCalendarRules = FiscalCalendarRules.getWeekDaysList();
        WeekDay[] weekDays = WeekDay.values();
        for (int i = 0; i < weekDays.length; i++)
           Assert.assertEquals(weekDaysFromFiscalCalendarRules.get(i).toString(), weekDays[i].name());
    }

    public void testGetWeekDayOffList() {
        String configWorkingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        setNewWorkingDays(configWorkingDays);
        List<Short> list = FiscalCalendarRules.getWeekDayOffList();
       Assert.assertEquals(list.size(), 2);
        Short dayOff = 1;
       Assert.assertEquals(list.get(0), dayOff);
        // set it back
        setSavedConfig();
    }

    public void testIsWorkingDay() {
        String configWorkingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        setNewWorkingDays(configWorkingDays);
        // get the supported ids for GMT-08:00 (Pacific Standard Time)
        String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
        // if no ids were returned, something is wrong. get out.
        // Otherwise get an id for Pacific Standard Time
        String pstId = ids[0];

        // create a Pacific Standard Time time zone
        SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, pstId);

        // set up rules for daylight savings time
        pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
        pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);

        // create a GregorianCalendar with the Pacific Daylight time zone
        // and the current date and time
        Calendar calendar = new GregorianCalendar(pdt);
        try {
            Locale savedLocale = Localization.getInstance().getMainLocale();
            new LocalizationConverter().setCurrentLocale(Locale.US);
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
            // Keith: DateFormat must be set to the same timezone as the
            // calendar
            // Otherwise dates don't roll over at the same exact time, causing
            // this and several other unit tests to fail
            df.setTimeZone(TimeZone.getTimeZone(pstId));
            df.applyPattern("yyyy/MM/dd");
            Date thursday = df.parse("2007/10/11");
            calendar.setTime(thursday);
            String out = thursday.toString();
            out.contains("A");
            new LocalizationConverter().setCurrentLocale(savedLocale);
        } catch (Exception e) {

        }

       Assert.assertTrue(FiscalCalendarRules.isWorkingDay(calendar));
        calendar.add(Calendar.DAY_OF_WEEK, 1); // Friday
       Assert.assertTrue(FiscalCalendarRules.isWorkingDay(calendar));
        calendar.add(Calendar.DAY_OF_WEEK, 1); // Sat
       Assert.assertTrue(!FiscalCalendarRules.isWorkingDay(calendar));
        calendar.add(Calendar.DAY_OF_WEEK, 1); // Sunday
       Assert.assertTrue(!FiscalCalendarRules.isWorkingDay(calendar));
        // set it back
        setSavedConfig();
    }

    public void testGetStartOfWeek() {
        Short startOfWeekDay = FiscalCalendarRules.getStartOfWeek();
        Short start = 2;
       Assert.assertEquals(startOfWeekDay, start);
    }

    public void testGetScheduleTypeForMeetingOnHoliday() {
        String scheduleType = FiscalCalendarRules.getScheduleTypeForMeetingOnHoliday();
       Assert.assertEquals(scheduleType.toUpperCase(), "same_day".toUpperCase());
    }

    public void testGetDaysForCalDefinition() {
        Short days = FiscalCalendarRules.getDaysForCalendarDefinition();
       Assert.assertEquals(days.shortValue(), 30);
    }

}
