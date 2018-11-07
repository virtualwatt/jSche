/*
 *  Copyright 2014 Vitaliy Tkachenko virtualvat@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package vvat.jsche.core.scheduleconfig;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Vitaliy Tkachenko
 */
public class ConfigCalculator {
	
	private static final Logger log = Logger.getLogger(ConfigCalculator.class);
	
	public static final long MS_IN_DAY = 24 * 60 * 60 * 1000;
	public static final long MS_IN_WEEK = MS_IN_DAY * 7;
	private static final long TOO_CLOSE_TIME_BUFFER = 1000; // 1 second to next event is too close

	/**
	 * Locate date/time in milliseconds for next dayOfWeek after date/time represented by afterTime milliseconds at specified time.
	 * If dayOfWeek is NULL then this day at specified time is located if possible on next day otherwise.
	 * @param afterTime
	 * @param dayOfWeek
	 * @param time
	 * @param timeZone zone in which time is specified
	 * @return MS for the located date/time
	 */
	public static long timeOfNextDay(long afterTime, DayOfWeek dayOfWeek, String time, String timeZone) {
		return timeOfNextDay(afterTime, mapDayOfWeek4Calendar(dayOfWeek), time, timeZone);
	}

	private static int mapDayOfWeek4Calendar(DayOfWeek dayOfWeek) {
		switch (dayOfWeek) {
		case SUNDAY:
			return Calendar.SUNDAY;
		case MONDAY:
			return Calendar.MONDAY;
		case TUESDAY:
			return Calendar.TUESDAY;
		case WEDNESDAY:
			return Calendar.WEDNESDAY;
		case THURSDAY:
			return Calendar.THURSDAY;
		case FRIDAY:
			return Calendar.FRIDAY;
		case SATURDAY:
			return Calendar.SATURDAY;
		default: // NULL
			return 0;
		}
	}

	/**
	 * See overloaded timeOfNextDay description
	 * @param afterTime
	 * @param calendarDay , 0 = the same day as afterTime if possible on next day
	 * @param time
	 * @param timeZone , used for "time"
	 * @return MS for the located date/time
	 */
	private static long timeOfNextDay(long afterTime, int calendarDay, String time, String timeZone) {
		Calendar calendar = timeZone == null ? Calendar.getInstance() : Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		calendar.setTimeInMillis(afterTime);
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (calendarDay != 0 && weekday != calendarDay)
		{
		    int days = calendarDay - weekday;
		    if (days < 0)
		    	days += 7;
		    calendar.add(Calendar.DAY_OF_YEAR, days);
		}
		long dayInMillis = calendar.getTimeInMillis();
		long timeInMillis = time2millis(time);
		long dateInMillis = dayInMillis + timeInMillis;
		if (afterTime > dateInMillis - TOO_CLOSE_TIME_BUFFER)
			dateInMillis += calendarDay == 0 ? MS_IN_DAY : MS_IN_WEEK;
		/*Date date = new Date(dateInMillis);
		String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		System.out.println(dateStr);*/
		return dateInMillis;
	}

	private static final Pattern timePtrn = Pattern.compile("^(\\d+):(\\d+)(?::(\\d+))?$");

	private static long time2millis(String time) {
		Matcher matcher = timePtrn.matcher(time);
		if (matcher.find()) {
			try {
				long hoursInSeconds = Long.parseLong(matcher.group(1)) * 3600;
				long minutesInSeconds = Long.parseLong(matcher.group(2)) * 60;
				String secondsStr = matcher.group(3);
				long seconds = secondsStr == null ? 0 : Long.parseLong(secondsStr);
				return (hoursInSeconds + minutesInSeconds + seconds) * 1000;
			}
			catch (NumberFormatException e) {
				String msg = "Invalid time: " + time;
				log.error(msg, e);
				throw new IllegalArgumentException(msg, e);
			}
		} else {
			String msg = "Invalid time: " + time;
			log.error(msg);
			throw new IllegalArgumentException("Invalid time: " + time);
		}
	}
}
