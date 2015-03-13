package northern.captain.vendingman.process;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Created by leo on 07.12.14.
 */
public class TimeProcessor
{

    /**
     * Constructs interval from first day to the last one of the month that has given dateTime
     * @param dateTime
     * @return interval for the month
     */
    Interval monthInterval(DateTime dateTime)
    {
        DateTime firstDate = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), 1, 0, 0);
        DateTime lastDate = firstDate.monthOfYear().addToCopy(1);

        return new Interval(firstDate, lastDate);
    }

    /**
     * Constructs time interval of one day for the given date
     * @param dateTime
     * @return interval for one whole day
     */
    Interval dayInterval(DateTime dateTime)
    {
        DateTime firstDate = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0);
        DateTime lastDate = firstDate.dayOfMonth().addToCopy(1);

        return new Interval(firstDate, lastDate);
    }

    /**
     * Get start of the month date relative to the given date and number of months
     * @param dateOfMonth - any date of the month
     * @param monthShift - number of months to shift (plus or minus)
     * @return
     */
    DateTime startMonth(DateTime dateOfMonth, int monthShift)
    {
        DateTime firstDate = new DateTime(dateOfMonth.getYear(), dateOfMonth.getMonthOfYear(), 1, 0, 0);

        if(monthShift != 0)
        {
            return firstDate.monthOfYear().addToCopy(monthShift);
        }

        return firstDate;
    }
}
