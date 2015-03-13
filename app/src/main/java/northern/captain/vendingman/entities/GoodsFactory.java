package northern.captain.vendingman.entities;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo on 13.03.15.
 */
public class GoodsFactory implements IEntityFactory<Goods>
{
    public static GoodsFactory instance = new GoodsFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_GOODS;
    }

    @Override
    public Goods newItem()
    {
        return new Goods();
    }

    @Override
    public Goods getById(int id)
    {
        try
        {
            return SQLManager.instance.getGoodsDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Goods item)
    {
        try
        {
            return SQLManager.instance.getGoodsDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(Goods item)
    {
        try
        {
            return SQLManager.instance.getGoodsDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Goods> getGoodsAll()
    {
        List<Goods> items;
        try
        {
            Dao<Goods, Integer> theDao = SQLManager.instance.getGoodsDao();
            QueryBuilder<Goods, Integer> builder = theDao.queryBuilder();
            builder.where().eq("state", 1);
            builder.orderBy("name", true);
            items = theDao.query(builder.prepare());
        }
        catch (SQLException ex)
        {
            Log.e("vend", "getGoodsAll", ex);
            items = new ArrayList<Goods>();
        }

        return items;
    }

    public Stakeholder createStakeholderByName(String shortName) throws SQLException
    {
        Stakeholder item = new Stakeholder();
        item.setName(shortName);

        SQLManager.instance.getStakeholderDao().create(item);

        return item;
    }

}
