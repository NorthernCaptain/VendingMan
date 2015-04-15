package northern.captain.vendingman.entities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 19.11.14.
 */
public class SQLManager
{
    public static SQLManager instance;

    public static void initialize()
    {
        instance = new SQLManager();
    }

    protected SQLManager()
    {
        initDB();
    }

    private String passwd = "";

    private static final String DB_NAME="vengingman.db";
    private static final int DB_VERSION = 3;

    private Helper helper;

    private static class Helper extends OrmLiteSqliteOpenHelper
    {
        boolean justCreated = false;

        public Helper(Context context)
        {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
        {
            try
            {
                TableUtils.createTable(connectionSource, User.class);
                TableUtils.createTable(connectionSource, Stakeholder.class);
                TableUtils.createTable(connectionSource, Order.class);
                TableUtils.createTable(connectionSource, OrderDetail.class);
                TableUtils.createTable(connectionSource, Replenishment.class);
                TableUtils.createTable(connectionSource, Maintenance.class);
                TableUtils.createTable(connectionSource, Goods.class);
                TableUtils.createTable(connectionSource, Accounting.class);
                TableUtils.createTable(connectionSource, VendingMachine.class);

                justCreated = true;
                SQLManager.instance.fillInitialDB();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion)
        {
            if(oldVersion < 2)
            {
                try
                {
                    database.execSQL("alter table replenishment add flags integer default 0");
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if(oldVersion < 3)
            {
                try
                {
                    database.execSQL("alter table orders add replenished_qty integer default 0");
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onOpen(SQLiteDatabase db)
        {
            super.onOpen(db);
            setPassword(db);
        }

        private void setPassword(SQLiteDatabase db)
        {
            //nothing now
        }
    }

    private void initDB()
    {
        helper = OpenHelperManager.getHelper(AndroidContext.mainActivity, Helper.class);
    }

    public boolean exists()
    {
        File dbf = AndroidContext.mainActivity.getDatabasePath(DB_NAME);
        return dbf.exists();
    }

    public boolean checkPassword(String passwd)
    {
        try
        {
            SQLiteDatabase ourDB = helper.getWritableDatabase();

            if(ourDB != null)
            {
                this.passwd = passwd;
                return true;
            }
        } catch(Exception ex) {}
        return false;
    }

    public SQLiteDatabase db()
    {
        return helper.getWritableDatabase();
    }

    public SQLiteDatabase dbr()
    {
        return helper.getReadableDatabase();
    }

    /**
     * Gets current date and time in Unix C time_t format
     * @return
     */
    public static long getCDate()
    {
        return new Date().getTime()/1000L;
    }

    public void shutdown()
    {
        OpenHelperManager.releaseHelper();
        helper = null;
        userDao = null;
        machineDao = null;
        goodsDao = null;
        maintenanceDao = null;
        replDao = null;
        accDao = null;
    }

    public void resume()
    {
        if(helper == null)
        {
            helper = OpenHelperManager.getHelper(AndroidContext.mainActivity, Helper.class);
        }
    }

    private void fillInitialDB() throws SQLException
    {
        if(!helper.justCreated)
        {
            return;
        }

//        ExpenseCategoryFactory categoryFactory = ExpenseCategoryFactory.instance;
//        categoryFactory.createByName("Продукты", null);
//        categoryFactory.createByName("Хоз. товары", null);
//        categoryFactory.createByName("Транспорт", null);
//        categoryFactory.createByName("Еда вне дома", null);
//        categoryFactory.createByName("Крупные покупки", null);
//
//        WalletFactory walletFactory = WalletFactory.instance;
//
//        walletFactory.createByShortName("Cash");
//        walletFactory.createByShortName("Card");

        UserFactory.instance.createByShortName("me");
        GoodsFactory.instance.createStakeholderByName("default");

        helper.justCreated = false;
    }
    
    // ---- DAO stuff
    

    private Dao<User, Integer> userDao;

    public Dao<User, Integer> getUserDao()
    {
        if(userDao == null)
        {
            try
            {
                userDao = helper.getDao(User.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return userDao;
    }

    private Dao<Goods, Integer> goodsDao;

    public Dao<Goods, Integer> getGoodsDao()
    {
        if(goodsDao == null)
        {
            try
            {
                goodsDao = helper.getDao(Goods.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return goodsDao;
    }

    private Dao<VendingMachine, Integer> machineDao;

    public Dao<VendingMachine, Integer> getMachineDao()
    {
        if(machineDao == null)
        {
            try
            {
                machineDao = helper.getDao(VendingMachine.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return machineDao;
    }

    private Dao<Maintenance, Integer> maintenanceDao;

    public Dao<Maintenance, Integer> getMaintenanceDao()
    {
        if(maintenanceDao == null)
        {
            try
            {
                maintenanceDao = helper.getDao(Maintenance.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return maintenanceDao;
    }

    private Dao<Stakeholder, Integer> stakeDao;

    public Dao<Stakeholder, Integer> getStakeholderDao()
    {
        if(stakeDao == null)
        {
            try
            {
                stakeDao = helper.getDao(Stakeholder.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return stakeDao;
    }

    private Dao<Replenishment, Integer> replDao;

    public Dao<Replenishment, Integer> getReplenishmentDao()
    {
        if(replDao == null)
        {
            try
            {
                replDao = helper.getDao(Replenishment.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return replDao;
    }

    private Dao<Accounting, Integer> accDao;

    public Dao<Accounting, Integer> getAccountingDao()
    {
        if(accDao == null)
        {
            try
            {
                accDao = helper.getDao(Accounting.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return accDao;
    }

    private Dao<Order, Integer> orderDao;

    public Dao<Order, Integer> getOrderDao()
    {
        if(orderDao == null)
        {
            try
            {
                orderDao = helper.getDao(Order.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return orderDao;
    }

    private Dao<OrderDetail, Integer> orderDetailDao;

    public Dao<OrderDetail, Integer> getOrderDetailDao()
    {
        if(orderDetailDao == null)
        {
            try
            {
                orderDetailDao = helper.getDao(OrderDetail.class);
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return orderDetailDao;
    }
}
