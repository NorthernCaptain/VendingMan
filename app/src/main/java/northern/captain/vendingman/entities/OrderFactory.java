package northern.captain.vendingman.entities;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leo on 15.04.15.
 */
public class OrderFactory implements IEntityFactory<Order>
{
    public static OrderFactory instance = new OrderFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_ORDER;
    }

    @Override
    public Order newItem()
    {
        Order order = new Order();
        order.setCreatedDate(new Date());
        return order;
    }

    @Override
    public Order getById(int id)
    {
        try
        {
            return SQLManager.instance.getOrderDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Order item)
    {
        try
        {
            return SQLManager.instance.getOrderDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(Order item)
    {
        try
        {
            return SQLManager.instance.getOrderDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public Order getLatestOrder()
    {
        try
        {
            Dao<Order, Integer> dao = SQLManager.instance.getOrderDao();

            QueryBuilder<Order, Integer> query = dao.queryBuilder();

            query.where().eq("state", 1);
            query.orderBy("created_date", false);

            query.limit(2L);

            List<Order> list = dao.query(query.prepare());
            if(list != null && !list.isEmpty())
            {
                return list.get(0);
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public List<Order> getOrderList()
    {
        try
        {
            Dao<Order, Integer> dao = SQLManager.instance.getOrderDao();

            QueryBuilder<Order, Integer> query = dao.queryBuilder();

            query.where().ne("state", 0);
            query.orderBy("created_date", false);

            List<Order> list = dao.query(query.prepare());
            if(list != null && !list.isEmpty())
            {
                return list;
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }

        return new ArrayList<Order>();
    }
}
