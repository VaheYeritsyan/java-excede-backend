package com.payment.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;


/**
 * 
 * A simple date converter class used to convert dates in
 * different types of formats for example:
 * A day, week and year input returns a date.
 * 
 * TODO: Needs to be more universal and dynamic, this was built for AEST timezone only.
 *  
 * @author Oska Jory <oska@excede.com.au>
 * @date 17th January 2023
 */
public class DateUtility {

	
	/**
	 * @param day - A day of week number 0 - 6.
	 * @param week - A week of year number 1 - 53
	 * @param year - A year
	 * @return A new {@link LocalDate} object based on the input day of week and week of year.
	 */
	public static LocalDate getDateOfDayOfWeek(int day, int week, int year) {
		
		if (day < 1 || day > 7) {
			throw new IllegalArgumentException("Day must be between 1 and 7.");
		}
		
		if (week < 1 || week > 53) {
			throw new IllegalArgumentException("Week must be between 1 and 53.");
		}
		
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		LocalDate date = LocalDate.ofYearDay(year, 1).with(weekFields.weekOfWeekBasedYear(), week)
				.with(weekFields.dayOfWeek(), day);
		
		return date;
	}
	
	
	/**
	 * @param date
	 * @param month
	 * @param year
	 * @return A new {@link LocalDate} object based on the input date, month and year. 
	 */
	public static LocalDate newDate(int date, int month, int year) {
		
		if (date < 1 || date > 31) {
			throw new IllegalArgumentException("Date must be between 1 and 31.");
		}
		
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("Month must be between 1 and 12.");
		}
		
		return LocalDate.of(year, month, date);
	}
	
	
	/**
	 * 
	 * Uses a pattern such as "dd-mm-yyyy" to parse
	 * the input date into a new LocalDate object.
	 * 
	 * Other pattern combinations might be:
	 * 
	 * "yyyy-mm-dd" (2023-01-23)
	 * "yyyy-MMM-dd" (2023-Jan-23)
	 * 
	 * etc.
	 * 
	 * Please read the {@link DateTimeFormatter} documentation for further pattern
	 * information.
	 * 
	 * @param date
	 * @param pattern
	 * @return A new {@link LocalDate} object based on the input date, parsed by the input pattern.
	 */
	public static LocalDate parse(String date, String pattern) {
		return LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
	}
	
	
	/**
	 * Input a 24 hour number between 0 and 24 and it will ensure it is returned 
	 * as a 2 digit string.
	 * 
	 * e.g. if 1 is input, 01 will be output. 
	 * If 10 is input 10 will be output.
	 * @param input
	 * @return A 24 hour number.
	 */
	public static String getHourFromString(String input) {
		String output = "";
		
		if (Integer.parseInt(input) > 23) {			
			input = "23";				
		} else if (Integer.parseInt(input) <= 0) {
			input = "0";
		}
		
		if (Integer.parseInt(input) < 10) {
			output+="0" + Integer.parseInt(input);
		} else {
			output = Integer.parseInt(input) + "";
		}
		
		return output;				
	}
	
	
	/**
	 * Input a 12 month number between 1 and 12 and it will ensure it is returned 
	 * as a 2 digit string.
	 * 
	 * e.g. if 1 is input, 01 will be output. 
	 * If 10 is input 10 will be output.
	 * @param input
	 * @return A month number.
	 */
	public static String getMonthFromString(String input) {
		String output = "";
		
		if (Integer.parseInt(input) > 12) {			
			input = "12";				
		} else if (Integer.parseInt(input) <= 1) {
			input = "1";
		}
		
		if (Integer.parseInt(input) < 10) {
			output+="0" + Integer.parseInt(input);
		} else {
			output = Integer.parseInt(input) + "";
		}
		
		return output;				
	}
	
	
	/**
	 * Input a 1 date number between 1 and 31 and it will ensure it is returned 
	 * as a 2 digit string.
	 * 
	 * e.g. if 1 is input, 01 will be output. 
	 * If 10 is input 10 will be output.
	 * @param input
	 * @return A date number.
	 */
	public static String getDateFromString(String input) {
		String output = "";
		
		if (Integer.parseInt(input) > 31) {			
			input = "31";				
		} else if (Integer.parseInt(input) <= 1) {
			input = "1";
		}
		
		if (Integer.parseInt(input) < 10) {
			output+="0" + Integer.parseInt(input);
		} else {
			output = Integer.parseInt(input) + "";
		}
		
		return output;				
	}
	
	
	/**
	 * @param date_string - Must be in yyyy-mm-dd format.
	 * @param hour
	 * @param minute
	 * @return A UTC String.
	 */
	public static String buildUTCString(String date_string, String hour, String minute) {
		return date_string + "T" + getHourFromString(hour) + ":" + getMinuteFromString(minute) + ":00.000Z";
	}
	
	
	/**
	 * Returns a UTC String from a custom input.
	 * @param date_string - The date in ISO-DATE format yyyy-mm-dd
	 * @param hour - The hour of the day (24 hour time)
	 * @param minute - The minute of the hour
	 * @param timezone - The timezone we are targeting.
	 * @return Returns a UTC timezone ISO string.
	 */
	public static String timezoneToUTCStringFromCustomInput(String date_string, String hour, String minute, String timezone) {
		// Removes the Z which declares it at Zero (UTC) time zone.
		return getUTCForDateTimeZoneString(timezone, buildUTCString(date_string, hour, minute).replace("Z",""), DateTimeFormatter.ISO_DATE_TIME);
		
	}
	
	
	/**
	 * @param year  
	 * @param month 
	 * @param date
	 * @param hour
	 * @param minute
	 * @return A UTC String. 
	 */
	public static String buildUTCString(String year, String month, String date, String hour, String minute) {
		return year + "-" + getMonthFromString(month) + "-" + getDateFromString(date) + "T" + getHourFromString(hour) + ":" + getMinuteFromString(minute) + ":00.000Z";
	}
	
	
	/**
	 * Input a minute number between 0 and 60 and it will ensure it is returned 
	 * as a 2 digit string.
	 * 
	 * e.g. if 1 is input, 01 will be output. 
	 * If 10 is input 10 will be output.
	 * @param input
	 * @return A 60 minute number.
	 */
	public static String getMinuteFromString(String input) {
		String output = "";
		
		if (Integer.parseInt(input) > 59) {			
			input = "59";				
		} else if (Integer.parseInt(input) <= 0) {
			input = "0";
		}
		
		if (Integer.parseInt(input) < 10) {
			output+="0" + Integer.parseInt(input);
		} else {
			output = Integer.parseInt(input) + "";
		}
		
		return output;				
	}
	
	
	
	
	/**
	 * Converts an input date String and Timezone to the UTC String version of that date and time.
	 * 
	 * For example 
	 * 
	 * 23/01/2023 10:20am Australia/Sydney will convert to 
	 * 
	 * 2023-01-22T11:20:00.000Z (UTC)
	 * 
	 * @param timezone
	 * @param date_time
	 * @param formatter
	 * @return A UTC date/time String in ISO_DATE_TIME format.
	 */
	public static String getUTCForDateTimeZoneString(String timezone, String date_time, DateTimeFormatter formatter) {
		return getUTCForDateTimeZone(timezone, date_time, formatter).format(DateTimeFormatter.ISO_DATE_TIME).toString().replace("[UTC]", "");
		
	}
	
	
	/**
	 * Converts an input date string and Timezone to the UTC version of that date and time.
	 * 
	 * For example 
	 * 
	 * 23/01/2023 10:20am Australia/Sydney will convert to 
	 * 
	 * 2023-01-22T11:20:00.000Z (UTC)
	 * 
	 * @param timezone
	 * @param date_time
	 * @param formatter
	 * @return A UTC date/time String in ISO_DATE_TIME format.
	 */
	public static ZonedDateTime getUTCForDateTimeZone(String timezone, String date_time, DateTimeFormatter formatter) {
		
        ZonedDateTime aestTime = ZonedDateTime.parse(date_time, formatter.withZone(ZoneId.of(timezone)));
        // Convert AEST time to UTC time
        ZonedDateTime utcTime = aestTime.withZoneSameInstant(ZoneId.of("UTC"));
        return utcTime;
	}
	
	
	
	/**
	 * @param inputDate - Date we are bouncing off.
	 * @param inputDay - The day of week we intend to bounce to. (1-7 Monday-Sunday)
	 * @return A {@link LocalDate} object of the new date that lands on the closest input day of week.
	 */
	 public static LocalDate getNearestDay(LocalDate inputDate, int inputDay) {

	        //Validate the input day number
	        if(inputDay < 1 || inputDay > 7){
	            throw new IllegalArgumentException("inputDay must be between 1 and 7");
	        }
	      
	        //Find the nearest date of the input day
	        return inputDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(inputDay)));
	  }

}