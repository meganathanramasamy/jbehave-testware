package com.group.jbehave.utilities;

import com.group.bdd.framework.LogUtil;
import org.apache.commons.lang.time.DateUtils;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.group.bdd.framework.Asserts.assertThat;

public class DateUtil {

    public static String findNextGivenDayFromDate(String currentDate, String dayToFind) {
        String orgDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = sdf.parse(currentDate);
            sdf.applyPattern("EEEE");
            String sMyDate = sdf.format(date);
            while (!sMyDate.equalsIgnoreCase(dayToFind)) {
                date = org.apache.commons.lang.time.DateUtils.addDays(date, 1);
                sMyDate = sdf.format(date);
            }
            sdf.applyPattern("yyyy-MM-dd");
            orgDate = sdf.format(date);
        } catch (Exception e) {
            assertThat("Error occurred. Exception: " + e.getMessage(), false);
        }
        return orgDate;
    }

    public static String changeDateFormat(String expDate) throws ParseException {
        SimpleDateFormat inSDF = new SimpleDateFormat("dd/mm/yyyy");
        SimpleDateFormat outSDF = new SimpleDateFormat("yyyy-mm-dd");
        String outDate = "";
        Date date = outSDF.parse(expDate);
        outDate = inSDF.format(date);
        return outDate;
    }

    public static String getDateTime(String reqFormat) {
        String returnDate = "";
        Format dateformat = new SimpleDateFormat("yyyy-MM-dd");
        Format timeformat = new SimpleDateFormat("HH:mm:ss.SSS");
        Format timeformatMilliSec = new SimpleDateFormat("HH:mm:ss:SSS");
        Format timeformatSec = new SimpleDateFormat("HH:mm:ss");

        Date sysDate = new Date();
        if (reqFormat.equals("yyyy-MM-ddTHH:mm:ss.SSSZ")) {
            returnDate = dateformat.format(sysDate) + "T" + timeformat.format(sysDate) + "Z";
        } else if (reqFormat.equals("yyyy-MM-ddTHH:mm:ss")) {
            returnDate = dateformat.format(sysDate) + "T" + timeformatSec.format(sysDate);
        } else if (reqFormat.equals("yyyy-MM-ddTHH:mm:ss:SSS")) {
            returnDate = dateformat.format(sysDate) + "T" + timeformatMilliSec.format(sysDate);
        } else {
            Format requiredFormat = new SimpleDateFormat(reqFormat);
            returnDate = requiredFormat.format(sysDate);
        }

        return returnDate;
    }

    private static Date getPlusTimeStamp(long millis) {
        Date datetime = new Date();
        try {
            Format timeformatSec = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            String strDate = timeformatSec.format(org.apache.commons.lang.time.DateUtils.addMilliseconds(new Date(), (int) millis));
            datetime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").parse(strDate);
        } catch (Exception e) {
            assertThat("ERRRRRRR", false);
        }
        return datetime;
    }

    public static Date getCurrTime(DateFormat sdf) {
        Date todayDate = new Date();
        Date currdate = null;
        String today = sdf.format(todayDate);
        try {
            currdate = sdf.parse(today);
        } catch (ParseException e) {
            assertThat("Error: " + e, false);
        }
        return currdate;
    }

    public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            assertThat("Error: " + ex, false);
        }
        return date != null;
    }

    public static String getcurDateWithFormat(String whichFormat) {
        Format dateformat = new SimpleDateFormat(whichFormat);
        Date sysDate = new Date();
        return dateformat.format(sysDate);
    }

    public static String generateNextDayExceptWeekEnds(String format) {
        Format nextDayFormat = new SimpleDateFormat(format);
        String nextDay = nextDayFormat.format(DateUtils.addDays(new Date(), 1));
        Date weekEndDates;
        try {
            weekEndDates = new SimpleDateFormat(format).parse(nextDay);
            String day = new SimpleDateFormat("EEEE").format(weekEndDates).toUpperCase();
            if (day.contains("SATURDAY")) {
                nextDay = nextDayFormat.format(DateUtils.addDays(weekEndDates, 2));
            } else if (day.contains("SUNDAY")) {
                nextDay = nextDayFormat.format(DateUtils.addDays(weekEndDates, 1));
            }
        } catch (ParseException e) {
            LogUtil.log(e.getMessage());
        }

        return nextDay;
    }

    public static String generateNextDayExceptSunday(String format) {
        Format nextDayFormat = new SimpleDateFormat(format);
        String nextDay = nextDayFormat.format(DateUtils.addDays(new Date(), 1));
        Date weekEndDates;
        try {
            weekEndDates = new SimpleDateFormat(format).parse(nextDay);
            String day = new SimpleDateFormat("EEEE").format(weekEndDates).toUpperCase();
            if (day.contains("SUNDAY")) {
                nextDay = nextDayFormat.format(DateUtils.addDays(weekEndDates, 1));
            }
        } catch (ParseException e) {
            LogUtil.log(e.getMessage());
        }

        return nextDay;
    }

    public static String getPlusOrMinusDate(String reqDate, String format) {
        String searchDate = "";
        Format yyMMDDFormat = new SimpleDateFormat(format);
        if (reqDate.equals("D")) {
            searchDate = getDateTime(format).toUpperCase();
        } else if (reqDate.contains("D+")) {
            reqDate = reqDate.substring(2);
            int day = Integer.parseInt(reqDate);
            searchDate = yyMMDDFormat.format(DateUtils.addDays(new Date(), day));
        } else if (reqDate.contains("D-")) {
            reqDate = reqDate.substring(1);
            int day = Integer.parseInt(reqDate);
            searchDate = yyMMDDFormat.format(DateUtils.addDays(new Date(), day));
        }
        return searchDate;
    }

    public static String generatePreviousDayExceptWeekEnds(String format, String checkDate) {
        try {
            Date weekEndDates = new SimpleDateFormat(format).parse(checkDate);
            String day = new SimpleDateFormat("EEEE").format(weekEndDates).toUpperCase();

            if (day.contains("SATURDAY")) {
                checkDate = new SimpleDateFormat(format).format(DateUtils.addDays(weekEndDates, -1));
            } else if (day.contains("SUNDAY")) {
                checkDate = new SimpleDateFormat(format).format(DateUtils.addDays(weekEndDates, -2));
            }
        } catch (Exception e) {
            assertThat("Exception: " + e.getMessage(), false);
        }

        return checkDate;
    }

    public static int getBusinessDays(int days, String format) {
        int holidays = 0;
        try {
            for (int i = 1; i <= days; i++) {
                int checkDate = Integer.parseInt("-" + i);
                String date = new SimpleDateFormat(format).format(DateUtils.addDays(new Date(), checkDate));
                Date weekEndDates = new SimpleDateFormat(format).parse(date);
                String day = new SimpleDateFormat("EEEE").format(weekEndDates).toUpperCase();
                if (day.contains("SATURDAY") || day.contains("SUNDAY")) {
                    holidays = holidays + 1;
                    days++;
                }
            }
        } catch (Exception e) {
            assertThat("Error: " + e.getMessage(), false);
        }

        return days;
    }

    public static String convertDateFormats(String date, String fromFormat, String toFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fromFormat);
        Date d1 = null;
        try {
            d1 = sdf.parse(date);
        } catch (ParseException e) {
            assertThat("Date Conversion Failed: " + e.getMessage(), false);
        }
        sdf.applyPattern(toFormat);
        date = sdf.format(d1);
        return date;
    }

    public static String convertDateFormat(String dString, String inFormat, String outFormat) {
        String dbFormatDate = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(inFormat);
            SimpleDateFormat dbSDF = new SimpleDateFormat(outFormat);

            Calendar c = Calendar.getInstance();
            Date date = sdf.parse(dString);
            c.setTime(date);
            dString = sdf.format(c.getTime());
            dbFormatDate = dbSDF.format(c.getTime());
        } catch (Exception e) {
            assertThat("Exception: " + e.getLocalizedMessage(), false);
        }
        return dbFormatDate;
    }


}
