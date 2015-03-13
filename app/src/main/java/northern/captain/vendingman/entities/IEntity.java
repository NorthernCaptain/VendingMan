package northern.captain.vendingman.entities;

import northern.captain.vendingman.tools.IJSONSerializer;

/**
 * Created by leo on 19.11.14.
 */
public interface IEntity
{
    public static final int STATE_OK = 1;
    public static final int STATE_DEAD = 0;

    public int getId();

    public char getType();
}
