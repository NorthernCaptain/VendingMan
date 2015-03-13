package northern.captain.vendingman.entities.criteria;

import java.util.Date;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 12.12.14.
 */
public class LessExpenseDateCrit extends BaseCriteria
{
    Date date;

    public LessExpenseDateCrit(Date date)
    {
        this.date = date;
    }

    @Override
    public String getAsString()
    {
        StringBuilder builder = new StringBuilder("tran_date < '");
        builder.append(AndroidContext.sqlFormat.format(date));
        builder.append('\'');

        return builder.toString();
    }
}
