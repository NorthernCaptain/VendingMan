package northern.captain.vendingman.entities;

import java.sql.SQLException;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 19.11.14.
 */
public class EntitiesFactory
{
    public static EntitiesFactory instance = new EntitiesFactory();

    public static void initialize()
    {
        instance.init();
    }

    protected EntitiesFactory()
    {
    }

    public void init()
    {
        UserFactory.instance.getType();

        {
            String uName = AndroidContext.mainActivity.setup.userName().get();
            AndroidContext.ourUser = UserFactory.instance.getByName(
                    uName);

            if(AndroidContext.ourUser == null)
            {
                try
                {
                    AndroidContext.ourUser = UserFactory.instance.createByShortName(uName);
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public IEntityFactory getFactoryByType(char type)
    {
        switch (type)
        {
            case IEntityFactory.TYPE_USER:
                return UserFactory.instance;

            default:
                return null;
        }
    }

}
