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
public class VendingMachineFactory implements IEntityFactory<VendingMachine>
{
    public static VendingMachineFactory instance = new VendingMachineFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_GOODS;
    }

    @Override
    public VendingMachine newItem()
    {
        return new VendingMachine();
    }

    @Override
    public VendingMachine getById(int id)
    {
        try
        {
            return SQLManager.instance.getMachineDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(VendingMachine item)
    {
        try
        {
            return SQLManager.instance.getMachineDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(VendingMachine item)
    {
        try
        {
            return SQLManager.instance.getMachineDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public List<VendingMachine> getVendingMachineAll()
    {
        List<VendingMachine> items;
        try
        {
            Dao<VendingMachine, Integer> theDao = SQLManager.instance.getMachineDao();
            QueryBuilder<VendingMachine, Integer> builder = theDao.queryBuilder();
            builder.where().eq("state", 1);
            builder.orderBy("name", true);
            items = theDao.query(builder.prepare());
        }
        catch (SQLException ex)
        {
            Log.e("vend", "getVendingMachineAll", ex);
            items = new ArrayList<VendingMachine>();
        }

        return items;
    }
}
