package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONException;
import org.json.JSONObject;

import northern.captain.vendingman.tools.IJSONContext;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "stakeholder")
public class Stakeholder implements IEntity
{
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "name", width = 128)
    public String name;

    /**
     * State of the record. 1 - OK, 0 - deleted
     */
    @DatabaseField(columnName = "state", defaultValue = "1")
    public int state = 1;

    @Override
    public char getType()
    {
        return IEntityFactory.TYPE_STAKEHOLDER;
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
}
