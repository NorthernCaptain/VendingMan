package northern.captain.vendingman.entities.criteria;

/**
 * Created by leo on 17.02.15.
 */
public class IDinListCrit extends BaseCriteria
{
    int[] ids;

    public IDinListCrit(int[] ids)
    {
        this.ids = ids;
    }

    @Override
    public String getAsString()
    {
        StringBuilder builder = new StringBuilder("id in (");
        for(int id : ids)
        {
            builder.append(id);
            builder.append(',');
        }
        builder.append("0)");

        return builder.toString();
    }
}
