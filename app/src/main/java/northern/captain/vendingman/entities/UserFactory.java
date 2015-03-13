package northern.captain.vendingman.entities;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 21.11.14.
 */
public class UserFactory implements IEntityFactory<User>
{
    public static UserFactory instance = new UserFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_USER;
    }

    @Override
    public User newItem()
    {
        return new User();
    }

    @Override
    public User getById(int id)
    {
        try
        {
            return SQLManager.instance.getUserDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(User item)
    {
        try
        {
            return SQLManager.instance.getUserDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public User createByShortName(String shortName) throws SQLException
    {
        User user = newItem();
        user.setName(shortName);

        SQLManager.instance.getUserDao().create(user);

        return user;
    }

    public User getByName(String name)
    {
        try
        {
            Dao<User, Integer> dao = SQLManager.instance.getUserDao();
            List<User> list = dao.queryForEq("name", name);
            if(list.isEmpty())
            {
                return null;
            }
            return list.get(0);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public Map<String, User> getUserAll()
    {
        Map<String, User> map = new HashMap<String, User>();
        try
        {
            List<User> users;
            users = SQLManager.instance.getUserDao().queryForAll();

            for(User user : users)
            {
                map.put(user.name, user);
            }
        }
        catch (SQLException ex)
        {
            Log.e("xpen", "getCategoryAll", ex);
        }

        return map;
    }

    @Override
    public boolean insert(User item)
    {
        try
        {
            return SQLManager.instance.getUserDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }


    public void changeCurrentUserName(String newUserName)
    {
        AndroidContext.ourUser.setName(newUserName);
        update(AndroidContext.ourUser);
        AndroidContext.mainActivity.setup.userName().put(newUserName);
    }
}
