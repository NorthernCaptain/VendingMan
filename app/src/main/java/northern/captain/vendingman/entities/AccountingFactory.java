package northern.captain.vendingman.entities;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leo on 27.03.15.
 */
public class AccountingFactory implements IEntityFactory<Accounting>
{
    public static AccountingFactory instance = new AccountingFactory();

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_ACCOUNTING;
    }

    @Override
    public Accounting newItem()
    {
        return new Accounting();
    }

    @Override
    public Accounting getById(int id)
    {
        try
        {
            return SQLManager.instance.getAccountingDao().queryForId(id);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Accounting item)
    {
        if(item.id == 0) return insert(item);

        try
        {
            return SQLManager.instance.getAccountingDao().update(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(Accounting item)
    {
        try
        {
            return SQLManager.instance.getAccountingDao().create(item) == 1;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    public Accounting getLatestAccounting(int machineId)
    {
        try
        {
            Dao<Accounting, Integer> dao = SQLManager.instance.getAccountingDao();

            QueryBuilder<Accounting, Integer> query = dao.queryBuilder();

            query.where().eq("machine_id", machineId).and().eq("state", 1);
            query.orderBy("created_date", false);

            query.limit(2L);

            List<Accounting> list = dao.query(query.prepare());
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

    public List<Accounting> getAccountingList(int machineId)
    {
        try
        {
            Dao<Accounting, Integer> dao = SQLManager.instance.getAccountingDao();

            QueryBuilder<Accounting, Integer> query = dao.queryBuilder();

            query.where().eq("machine_id", machineId).and().eq("state", 1);
            query.orderBy("created_date", false);

            List<Accounting> list = dao.query(query.prepare());
            if(list != null && !list.isEmpty())
            {
                return list;
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }

        return new ArrayList<Accounting>();
    }

    public List<Accounting> getAccountingListByDates(int machineId, Date from, Date to)
    {
        try
        {
            Dao<Accounting, Integer> dao = SQLManager.instance.getAccountingDao();

            QueryBuilder<Accounting, Integer> query = dao.queryBuilder();

            query.where().eq("machine_id", machineId).and().eq("state", 1)
                    .and().ge("created_date", from).and().lt("created_date", to);
            query.orderBy("created_date", false);

            List<Accounting> list = dao.query(query.prepare());
            if(list != null && !list.isEmpty())
            {
                return list;
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }

        return new ArrayList<Accounting>();
    }
}
