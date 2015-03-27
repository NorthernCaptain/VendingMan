package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "maintenance")
public class Maintenance implements IEntity
{
    public static final String STATUS_OPEN = "op";
    public static final String STATUS_DONE = "do";

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "machine_id", defaultValue = "0")
    public int machineId;

    @DatabaseField(columnName = "user_id", defaultValue = "0")
    public int userId;

    /**
     * Status of the maintenance: op - Open, fi - Done
     */
    @DatabaseField(columnName = "status", width = 2, defaultValue = "op")
    public String status;

    @DatabaseField(columnName = "comments")
    public String comments;

    @DatabaseField(columnName = "start_date", dataType = DataType.DATE_STRING)
    public Date startDate;

    @DatabaseField(columnName = "finish_date", dataType = DataType.DATE_STRING)
    public Date finishDate;

    @DatabaseField(columnName = "replenished_qty", defaultValue = "0")
    public int replenishedQty;

    @DatabaseField(columnName = "has_supply", defaultValue = "0")
    public int hasSupply;

    /**
     * State of the record. 1 - OK, 0 - deleted
     */
    @DatabaseField(columnName = "state", defaultValue = "1")
    public int state = 1;

    public Object extra;

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_MAINTENANCE;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getComments()
    {
        return comments;
    }

    public void setComments(String comments)
    {
        this.comments = comments;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getFinishDate()
    {
        return finishDate;
    }

    public void setFinishDate(Date finishDate)
    {
        this.finishDate = finishDate;
    }

    public int getReplenishedQty()
    {
        return replenishedQty;
    }

    public void setReplenishedQty(int replenished_qty)
    {
        this.replenishedQty = replenished_qty;
    }

    public int getHasSupply()
    {
        return hasSupply;
    }

    public void setHasSupply(int hasSupply)
    {
        this.hasSupply = hasSupply;
    }

    public boolean isOpen() { return STATUS_OPEN.equals(status);}
}
