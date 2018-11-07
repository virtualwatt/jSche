package vvat.jsche.scheduleconfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;
import vvat.jsche.core.scheduleconfig.ConfigCalculator;
import vvat.jsche.core.scheduleconfig.DayOfWeek;

public class CalendarCalculationTest extends TestCase {

    /*public CalendarCalculationTests( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( CalendarCalculationTests.class );
    }*/
	
	private static final String testDatesStrFormat = "yyyy-MM-dd HH:mm:ss";
	private static final String inoutDateTimeZone = "EET";	// GMT+2 winter, GMT+3 summer
	private static final SimpleDateFormat inoutDateFormat = new SimpleDateFormat(testDatesStrFormat);
	
	static {
		TimeZone timeZone = TimeZone.getTimeZone(inoutDateTimeZone);
		inoutDateFormat.setTimeZone(timeZone);
	}

    public void testIntervalCalculations() throws ParseException
    {
    	String[] inDates = new String[]		// 2014-05-26 = Monday, ... ; EET
    			{"2014-05-26 18:32:04", "2014-05-26 11:32:04", "2014-05-25 01:32:04", "2014-05-28 21:32:04"};
    	DayOfWeek nextDay = DayOfWeek.MONDAY;
    	String timeInDay = "13:15";			// UTC, = 15:15 winter EET or 16:15 summer EET
    	String[] outDates = new String[]	// Monday, 16:15 EET = 13:15 UTC
    			{"2014-06-02 16:15:00", "2014-05-26 16:15:00", "2014-05-26 16:15:00", "2014-06-02 16:15:00"};

    	// next day of week tests
		for (int i = 0; i < inDates.length; i++) {
			executeIntervalCalculationsTest(inDates[i], nextDay, timeInDay, "UTC", outDates[i]);
		}

		// this or next day tests
		executeIntervalCalculationsTest(
				"2014-05-26 13:32:04",			// EET
				DayOfWeek.NULL, "12:00", "UTC",	// =14:00 winter EET or 15:00 summer EET
				"2014-05-26 15:00:00");			// EET
		executeIntervalCalculationsTest(
				"2014-05-26 15:32:04",			// EET
				DayOfWeek.NULL, "12:00", "UTC",
				"2014-05-27 15:00:00");			// EET

		executeIntervalCalculationsTest(
				"2014-05-26 13:32:04",			// EET
				DayOfWeek.NULL, "14:00", "EET",	// =14:00 winter EET or 15:00 summer EET
				"2014-05-26 14:00:00");			// EET
		executeIntervalCalculationsTest(
				"2014-05-26 15:32:04",			// EET
				DayOfWeek.NULL, "14:00", "EET",
				"2014-05-27 14:00:00");			// EET

		executeIntervalCalculationsTest(
				"2014-05-26 15:32:04",			// EET
				DayOfWeek.NULL, "14:00:33", "EET",
				"2014-05-27 14:00:33");			// EET
    }

	/**
	 * Test if the <code>outDate</code> is the correct first timestamp to
	 * schedule after the <code>inDate</code> for the given parameters to find
	 * the next time accordingly to the <code>nextDay</code> and the
	 * <code>timeInDay</code> for the given <code>timeZone</code>
	 * 
	 * @param inDate
	 * @param nextDay
	 * @param timeInDay
	 * @param timeZone
	 * @param outDate
	 * @throws ParseException
	 */
	private void executeIntervalCalculationsTest(String inDate, DayOfWeek nextDay, String timeInDay, String timeZone, String outDate)
			throws ParseException {
		Date inDateDate = inoutDateFormat.parse(inDate);
		long sinceTime = inDateDate.getTime();
		// look for next <nextDay> day of the week at <timeInDay> time after <sinceTime> date
		long timeOfNextDay = ConfigCalculator.timeOfNextDay(sinceTime, nextDay, timeInDay, timeZone);
		Date date = new Date(timeOfNextDay);
		String dateStr = inoutDateFormat.format(date);
		/*StringBuilder sb = new StringBuilder();
		sb.append("\tTest:\n").append(inDate).append('\n').append(nextDay).append('\n').append(timeInDay).
				append("\nExpected: ").append(outDate).append("\nActual: ").append(dateStr);
		System.out.println(sb.toString());*/
		assertEquals(dateStr, outDate);
	}

}
