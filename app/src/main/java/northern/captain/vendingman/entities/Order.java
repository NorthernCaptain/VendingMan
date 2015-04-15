package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.R;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "orders")
public class Order implements IEntity
{
    public static final int STATE_OK = 1;
    public static final int STATE_DONE = 2;
    public static final int STATE_DELETED = 0;

    @DatabaseField(generatedId = true)
    public int id;

    /**
     * Ref to user.id
     */
    @DatabaseField(columnName = "user_id", defaultValue = "0")
    public int userId;

    /**
     * Ref to stakeholder.id
     */
    @DatabaseField(columnName = "supplier_id", defaultValue = "0")
    public int supplierId;

    @DatabaseField(columnName = "comments")
    public String comments;

    @DatabaseField(columnName = "created_date", dataType = DataType.DATE_STRING)
    public Date createdDate;

    @DatabaseField(columnName = "shared_date", dataType = DataType.DATE_STRING)
    public Date sharedDate;

    /**
     * State of the record. 1 - OK, 0 - deleted, 2 - closed
     */
    @DatabaseField(columnName = "state", defaultValue = "1")
    public int state = STATE_OK;

    @DatabaseField(columnName = "replenished_qty", defaultValue = "0")
    public int replenishedQty;

    public Object extra;

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_ORDER;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public int getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(int supplierId)
    {
        this.supplierId = supplierId;
    }

    public String getComments()
    {
        return comments;
    }

    public String getName()
    {
        return AndroidContext.mainActivity.getResources().getString(R.string.order_num) + id;
    }

    public boolean isOpen()
    {
        return state == STATE_OK;
    }

    public void setComments(String comments)
    {
        this.comments = comments;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    public Date getSharedDate()
    {
        return sharedDate;
    }

    public void setSharedDate(Date sharedDate)
    {
        this.sharedDate = sharedDate;
    }
}
