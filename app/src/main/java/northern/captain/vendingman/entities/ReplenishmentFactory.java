package northern.captain.vendingman.entities;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo on 18.03.15.
 */
public class ReplenishmentFactory implements IEntityFactory<Replenishment>
{
    public static ReplenishmentFactory instance = new ReplenishmentFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_REPLENISHMENT;
    }

    @Override
    public Replenishment newItem()
    {
        return new Replenishment();
    }

    @Override
    public Replenishment getById(int id)
    {
        try
        {
            return SQLManager.instance.getReplenishmentDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Replenishment item)
    {
        if(item.id == 0)
        {
            return insert(item);
        }

        try
        {
            return SQLManager.instance.getReplenishmentDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(Replenishment item)
    {
        try
        {
            return SQLManager.instance.getReplenishmentDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Replenishment> getReplenishments(int maintenanceId)
    {
        try
        {
            Dao<Replenishment, Integer> dao = SQLManager.instance.getReplenishmentDao();

            QueryBuilder<Replenishment, Integer> query = dao.queryBuilder();

            query.where().eq("main_id", maintenanceId).and().eq("state", 1);

            List<Replenishment> list = dao.query(query.prepare());
            if(list != null)
            {
                return list;
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }
}
