package northern.captain.vendingman.entities;

/**
 * Created by leo on 23.04.15.
 */
public class OrderDetItem
{
    public Goods goods;
    public OrderDetail orderDetail;
    public int qty;
    public Object extra;

    public void setOrderDetail(OrderDetail ord)
    {
        orderDetail = ord;
        qty = ord == null ? 0 : orderDetail.qty;
    }

    public void setGoods(Goods goods)
    {
        this.goods = goods;
    }

    public int getQty() { return qty;}
}
