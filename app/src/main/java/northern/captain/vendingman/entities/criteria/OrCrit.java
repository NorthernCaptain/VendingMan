package northern.captain.vendingman.entities.criteria;

/**
 * Created by leo on 09.12.14.
 */
public class OrCrit extends BaseCriteria
{
    ICriteria one, two;

    public OrCrit(ICriteria one, ICriteria two)
    {
        this.one = one;
        this.two = two;
    }

    @Override
    public String getAsString()
    {
        StringBuilder buf = new StringBuilder("(");
        buf.append(one.getAsString());
        buf.append(" OR ");
        buf.append(two.getAsString());
        buf.append(")");
        return buf.toString();
    }

}
