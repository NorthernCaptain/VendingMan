package northern.captain.vendingman.entities;

/**
 * Created by leo on 19.11.14.
 */
public interface IEntityFactory<EntityType extends IEntity>
{
    public static final char TYPE_ACCOUNTING = 'A';
    public static final char TYPE_ORDER = 'O';
    public static final char TYPE_ORDER_DETAIL = 'D';
    public static final char TYPE_REPLENISHMENT = 'R';
    public static final char TYPE_MAINTENANCE = 'M';
    public static final char TYPE_GOODS = 'G';
    public static final char TYPE_MACHINE = 'V';
    public static final char TYPE_STAKEHOLDER = 'S';
    public static final char TYPE_USER = 'U';

    public char       getType();
    public EntityType newItem();
    public EntityType getById(int id);
    public boolean    update(EntityType item);
    public boolean    insert(EntityType item);
}
