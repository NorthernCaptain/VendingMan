package northern.captain.vendingman.entities;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by leo on 18.03.15.
 */
public class MaintenanceFactory implements IEntityFactory<Maintenance>
{
    public static MaintenanceFactory instance = new MaintenanceFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_MAINTENANCE;
    }

    @Override
    public Maintenance newItem()
    {
        return new Maintenance();
    }

    @Override
    public Maintenance getById(int id)
    {
        try
        {
            return SQLManager.instance.getMaintenanceDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Maintenance item)
    {
        try
        {
            return SQLManager.instance.getMaintenanceDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(Maintenance item)
    {
        try
        {
            return SQLManager.instance.getMaintenanceDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public Maintenance getLatestMaintenance(int machineId)
    {
        try
        {
            Dao<Maintenance, Integer> dao = SQLManager.instance.getMaintenanceDao();

            QueryBuilder<Maintenance, Integer> query = dao.queryBuilder();

            query.where().eq("machine_id", machineId);
            query.orderBy("start_date", false);

            query.limit(2L);

            List<Maintenance> list = dao.query(query.prepare());
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
}
