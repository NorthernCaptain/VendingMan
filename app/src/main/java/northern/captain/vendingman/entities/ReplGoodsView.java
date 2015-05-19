package northern.captain.vendingman.entities;

import java.util.Date;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 24.04.15.
 */
public class ReplGoodsView
{
    public int replenishmentId;
    public int goodsId;
    public String goodsName;
    public int qty;
    public Date startDate;
    public String startDateS;

    public int sumQty = 0;

    public void init()
    {
        startDateS = AndroidContext.repDateFormat.format(startDate);
    }

    public String getKey()
    {
        return startDateS + goodsId;
    }
}
