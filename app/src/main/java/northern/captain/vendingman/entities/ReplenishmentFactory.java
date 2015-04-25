package northern.captain.vendingman.entities;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import northern.captain.vendingman.AndroidContext;

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

    public List<ReplGoodsView> getReplenishmentsForMachine(int machineId, Date from, Date to)
    {
        List<ReplGoodsView> list = new ArrayList<ReplGoodsView>();

        Dao<Replenishment, Integer> dao = SQLManager.instance.getReplenishmentDao();

        try
        {
            StringBuilder buf = new StringBuilder(
                    "select replenishment.id, replenishment.goods_id, goods.name, replenishment.qty, " +
                            "maintenance.start_date " +
                            "from maintenance join replenishment on maintenance.id = replenishment.main_id " +
                            "join goods on replenishment.goods_id = goods.id " +
                            "where maintenance.state = 1 and replenishment.qty>0 and maintenance.machine_id=");
            buf.append(machineId);
            buf.append(" and maintenance.start_date>='");
            buf.append(AndroidContext.sqlFormat.format(from));
            buf.append("' and maintenance.start_date<'");
            buf.append(AndroidContext.sqlFormat.format(to));
            buf.append("'");

            GenericRawResults<Object[]> results =
                    dao.queryRaw(buf.toString(),
                            new DataType[]{DataType.INTEGER, DataType.INTEGER, DataType.STRING, DataType.INTEGER,
                                    DataType.DATE_STRING, DataType.INTEGER }
                    );

            for(Object[] row : results)
            {
                ReplGoodsView view = new ReplGoodsView();
                view.replenishmentId = (int)row[0];
                view.goodsId = (int)row[1];
                view.goodsName = (String)row[2];
                view.qty = (int)row[3];
                view.startDate = (Date)row[4];

                view.init();

                list.add(view);
            }

            results.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        return list;
    }
}
