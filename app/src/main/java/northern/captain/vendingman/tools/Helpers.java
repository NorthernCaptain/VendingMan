package northern.captain.vendingman.tools;

import org.joda.time.MutableDateTime;

import java.util.Date;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 21.11.14.
 */
public class Helpers
{
    public static final double MULTIPLIER = 100;
    public static int amountFromString(String amountString)
    {
        return Integer.parseInt(amountString) * (int)MULTIPLIER;
    }

    private static final long MILLIS_PER_DAY = 24 * 3600 * 1000;

    private static MutableDateTime dateTime1 = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0);
    private static MutableDateTime dateTime2 = new MutableDateTime(2000, 1, 1, 0, 0, 0, 0);


    public static final boolean isSameDay(Date date1, Date date2)
    {
        if(date1 == null || date2 == null)
            return false;

        dateTime1.setDate(date1.getTime());
        dateTime2.setDate(date2.getTime());

        return dateTime1.equals(dateTime2);
//        return date1.getTime() / MILLIS_PER_DAY == date2.getTime() / MILLIS_PER_DAY;
    }

    public static final String amountToString(int amount)
    {
        return String.valueOf(((double)amount) / MULTIPLIER);
    }

    public static final String smartDateString(Date date)
    {
        long now = System.currentTimeMillis() / MILLIS_PER_DAY;
        long yesterday = now - 1;
        long thatDay = date.getTime() / MILLIS_PER_DAY;

        if(now == thatDay)
        {
            return AndroidContext.mainActivity.today;
        }

        if(yesterday == thatDay)
        {
            return AndroidContext.mainActivity.yesterday;
        }

        return AndroidContext.dateFormat.format(date);
    }

    public static String trunc(String source, int len)
    {
        if(source.length() < len)
        {
            return source;
        }
        return source.substring(0, len);
    }

    public static boolean isNullOrEmpty(String string)
    {
        return string == null || string.length() == 0;
    }
}
