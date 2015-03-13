package northern.captain.vendingman.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import northern.captain.vendingman.entities.IEntity;
import northern.captain.vendingman.entities.IEntityFactory;

/**
 * Created by leo on 21.11.14.
 */
@DatabaseTable(tableName = "replenishment")
public class Replenishment implements IEntity
{
    @DatabaseField(generatedId = true)
    public int id;

    /**
     * Ref to maintenance.id
     */
    @DatabaseField(columnName = "main_id", defaultValue = "0")
    public int mainId;

    @DatabaseField(columnName = "goods_id", defaultValue = "0")
    public int goodsId;

    @DatabaseField(columnName = "qty", defaultValue = "0")
    public int qty;

    @DatabaseField(columnName = "comments")
    public String comments;

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
        return IEntityFactory.TYPE_REPLENISHMENT;
    }

    @Override
    public int getId()
    {
        return id;
    }

    public int getMainId()
    {
        return mainId;
    }

    public void setMainId(int mainId)
    {
        this.mainId = mainId;
    }

    public int getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(int goodsId)
    {
        this.goodsId = goodsId;
    }

    public int getQty()
    {
        return qty;
    }

    public void setQty(int qty)
    {
        this.qty = qty;
    }

    public String getComments()
    {
        return comments;
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
}
