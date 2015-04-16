package northern.captain.vendingman.entities;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leo on 16.04.15.
 */
public class OrderDetailFactory implements IEntityFactory<OrderDetail>
{
    public static OrderDetailFactory instance = new OrderDetailFactory();
    @Override
    public char getType()
    {
        return TYPE_ORDER_DETAIL;
    }

    @Override
    public OrderDetail newItem()
    {
        OrderDetail detail = new OrderDetail();
        detail.setCreatedDate(new Date());
        return detail;
    }

    @Override
    public OrderDetail getById(int id)
    {
        try
        {
            return SQLManager.instance.getOrderDetailDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(OrderDetail item)
    {
        if(item.id == 0)
        {
            return insert(item);
        }

        try
        {
            return SQLManager.instance.getOrderDetailDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(OrderDetail item)
    {
        try
        {
            return SQLManager.instance.getOrderDetailDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public List<OrderDetail> getOrderDetails(int orderId)
    {
        try
        {
            Dao<OrderDetail, Integer> dao = SQLManager.instance.getOrderDetailDao();

            QueryBuilder<OrderDetail, Integer> query = dao.queryBuilder();

            query.where().eq("order_id", orderId).and().eq("state", 1);

            List<OrderDetail> list = dao.query(query.prepare());
            if (list != null)
            {
                return list;
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }
}
