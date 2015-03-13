package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "accounting")
public class Accounting implements IEntity
{
    @DatabaseField(generatedId = true)
    public int id;

    /**
     * Ref to machine.id
     */
    @DatabaseField(columnName = "machine_id", defaultValue = "0")
    public int machineId;

    /**
     * Ref to user.id
     */
    @DatabaseField(columnName = "user_id", defaultValue = "0")
    public int userId;

    @DatabaseField(columnName = "comments")
    public String comments;

    @DatabaseField(columnName = "coins_qty", defaultValue = "0")
    public int coinsQty;

    @DatabaseField(columnName = "money_qty", defaultValue = "0")
    public int moneyQty;

    @DatabaseField(columnName = "other_qty", defaultValue = "0")
    public int otherQty;

    @DatabaseField(columnName = "created_date", dataType = DataType.DATE_STRING)
    public Date createdDate;

    /**
     * State of the record. 1 - OK, 0 - deleted
     */
    @DatabaseField(columnName = "state", defaultValue = "1")
    public int state = 1;

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_ACCOUNTING;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public int getMachineId()
    {
        return machineId;
    }

    public void setMachineId(int machineId)
    {
        this.machineId = machineId;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public String getComments()
    {
        return comments;
    }

    public void setComments(String comments)
    {
        this.comments = comments;
    }

    public int getCoinsQty()
    {
        return coinsQty;
    }

    public void setCoinsQty(int coinsQty)
    {
        this.coinsQty = coinsQty;
    }

    public int getMoneyQty()
    {
        return moneyQty;
    }

    public void setMoneyQty(int moneyQty)
    {
        this.moneyQty = moneyQty;
    }

    public int getOtherQty()
    {
        return otherQty;
    }

    public void setOtherQty(int otherQty)
    {
        this.otherQty = otherQty;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }
}
