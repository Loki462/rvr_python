/**
 * 
 */
package utilities;

import java.time.LocalDateTime;


/**
 * @author n691581
 *
 */
public class DateUtil {
	
	public String dateTimeDDMMYYYYHHMMSS = null;;
	private static LocalDateTime timePoint = LocalDateTime.now();
	private static int date = timePoint.getDayOfMonth();
	private static int month = timePoint.getMonthValue();
	private static int year = timePoint.getYear();
	private static int hour = timePoint.getHour();
	private static int minute = timePoint.getMinute();
	private static int second = timePoint.getSecond();
	
	/**
	 * Constructor
	 */
	public DateUtil() {
		dateTimeDDMMYYYYHHMMSS = getCurrentDateTime();
	}
	
	/**
	 * Return current date & time in dd-mm-yyyy-hh-mm-ss format
	 */
	public static String getCurrentDateTime() {	
		String dateTime = date + "-" + month + "-" + year + "-" + hour + "-" + minute + "-" + second;		
		return dateTime;  		
	}
	
	
	
}
