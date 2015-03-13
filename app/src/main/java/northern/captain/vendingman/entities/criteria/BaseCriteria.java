package northern.captain.vendingman.entities.criteria;

/**
 * Created by leo on 09.12.14.
 */
public abstract class BaseCriteria implements ICriteria
{
    public BaseCriteria nextCriteria;

    public void add(BaseCriteria newCriteria)
    {
        newCriteria.nextCriteria = nextCriteria;
        nextCriteria = newCriteria;
    }
}
