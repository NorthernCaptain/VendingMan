package northern.captain.vendingman.entities.criteria;

/**
 * Created by leo on 09.12.14.
 */
public class LikeExpenseDescCrit extends BaseCriteria
{
    String filter;

    public LikeExpenseDescCrit(String filter)
    {
        this.filter = filter;
    }

    @Override
    public String getAsString()
    {
        StringBuilder buf = new StringBuilder("expenses.description like '%");
        buf.append(filter);
        buf.append("%'");
        return buf.toString();
    }
}
