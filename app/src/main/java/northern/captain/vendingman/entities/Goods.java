package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "goods")
public class Goods implements IEntity
{
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "name", width = 256)
    public String name;

    @DatabaseField(columnName = "description")
    public String description;

    /**
     * Supplier of this goods
     */
    @DatabaseField(columnName = "stakeholder_id", defaultValue = "1")
    public int stakeholderId = 1;

    @DatabaseField(columnName = "tags", width = 64)
    public String tags;

    /**
     * State of the record. 1 - OK, 0 - deleted
     */
    @DatabaseField(columnName = "state", defaultValue = "1")
    public int state = 1;

    public Object extra;

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_GOODS;
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
}
