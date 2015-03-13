package northern.captain.vendingman.entities.criteria;

import android.util.Log;

/**
 * Created by leo on 09.12.14.
 */
public class SearchContext
{
    public BaseCriteria criteria;
    public int limitRows = 0;

    public String getWhereString()
    {
        if(criteria == null)
        {
            return null;
        }

        BaseCriteria currentCriteria = criteria;

        StringBuilder buf = new StringBuilder(" (");

        while(currentCriteria != null)
        {
            buf.append(currentCriteria.getAsString());
            currentCriteria = currentCriteria.nextCriteria;
            if(currentCriteria != null)
            {
                buf.append(" AND ");
            }
        }

        buf.append(") ");

        String ret = buf.toString();

        Log.i("xpense", "SQL Criteria: " + ret);

        return buf.toString();
    }

    public SearchContext add(BaseCriteria criteria)
    {
        if(this.criteria == null)
        {
            this.criteria = criteria;
        }
        else
        {
            this.criteria.add(criteria);
        }

        return this;
    }
}
