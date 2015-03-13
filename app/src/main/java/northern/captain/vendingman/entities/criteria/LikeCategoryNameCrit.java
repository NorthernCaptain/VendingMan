package northern.captain.vendingman.entities.criteria;

/**
 * Created by leo on 09.12.14.
 */
public class LikeCategoryNameCrit extends BaseCriteria
{
    String filter;

    public LikeCategoryNameCrit(String filter)
    {
        this.filter = filter;
    }

    @Override
    public String getAsString()
    {
        StringBuilder buf = new StringBuilder("ecategory.name like '%");
        buf.append(filter);
        buf.append("%'");
        return buf.toString();
    }
}
