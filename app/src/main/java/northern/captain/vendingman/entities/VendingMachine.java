package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "machine")
public class VendingMachine implements IEntity
{
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "name", width = 256)
    public String name;

    @DatabaseField(columnName = "description")
    public String description;

    @DatabaseField(columnName = "tags", width = 128)
    public String tags;

    @DatabaseField(columnName = "last_maintain_date", dataType = DataType.DATE_STRING)
    public Date lastMaintainDate;

    @DatabaseField(columnName = "last_account_date", dataType = DataType.DATE_STRING)
    public Date lastAccountDate;


    public Object extra;
    /**
     * State of the record. 1 - OK, 0 - deleted
     */
    @DatabaseField(columnName = "state", defaultValue = "1")
    public int state = 1;

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_MACHINE;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String shortName)
    {
        this.name = shortName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public Date getLastMaintainDate()
    {
        return lastMaintainDate;
    }

    public void setLastMaintainDate(Date lastMaintainDate)
    {
        this.lastMaintainDate = lastMaintainDate;
    }

    public Date getLastAccountDate()
    {
        return lastAccountDate;
    }

    public void setLastAccountDate(Date lastAccountDate)
    {
        this.lastAccountDate = lastAccountDate;
    }
}
