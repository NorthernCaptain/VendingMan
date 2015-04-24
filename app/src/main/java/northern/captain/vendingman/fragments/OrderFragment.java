package northern.captain.vendingman.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.R;
import northern.captain.vendingman.dialogs.EnterTextStringDialog;
import northern.captain.vendingman.entities.Goods;
import northern.captain.vendingman.entities.GoodsFactory;
import northern.captain.vendingman.entities.Order;
import northern.captain.vendingman.entities.OrderDetItem;
import northern.captain.vendingman.entities.OrderDetail;
import northern.captain.vendingman.entities.OrderDetailFactory;
import northern.captain.vendingman.entities.OrderFactory;
import northern.captain.vendingman.gui.RepeatListenerAbstract;
import northern.captain.vendingman.reports.OrderReport;
import northern.captain.vendingman.tools.Helpers;
import northern.captain.vendingman.tools.MyToast;

/**
 * Created by leo on 3/20/15.
 */
@EFragment(R.layout.frag_order)
public class OrderFragment extends BaseFragment
{
    @ViewById(R.id.order_dates_lb)
    TextView orderTopText;

    @ViewById(R.id.order_comment_edit)
    EditText orderCommentEdit;

    @ViewById(R.id.order_status_lb)
    TextView orderStatusText;

    @ViewById(R.id.order_repl_qty_lb)
    TextView orderQtyText;

    @ViewById(R.id.order_num_lbl)
    TextView orderNumText;

    @ViewById(R.id.order_all_rview)
    TwoWayView listView;

    @ViewById(R.id.order_selected_rview)
    TwoWayView usedListView;

    @ViewById(R.id.order_comment_lay)
    View commentLay;

    @ViewById(R.id.order_stop)
    ImageButton stopBut;

    private boolean locked = false;

    private static final int MODE_LIST_ALL = 1;
    private static final int MODE_LIST_USED = 2;
    private static final int MODE_COMMENTS = 3;

    private int mode = MODE_LIST_ALL;

    private Order order;
    public void setOrder(Order order)
    {
        this.order = order;
    }

    List<Goods> goodsItems;

    Set<OrderDetItemVisual> itemsToUpdate = new HashSet<OrderDetItemVisual>();

    public class OrderDetItemVisual extends OrderDetItem
    {
        void setQty(int newQty)
        {
            qty = newQty < 0 ? 0 : newQty;

            if(orderDetail == null)
            {
                orderDetail = OrderDetailFactory.instance.newItem();
                orderDetail.setOrderId(order.id);
                orderDetail.goodsId = goods.id;
            }

            orderDetail.setQty(qty);
            orderDetail.state = qty > 0 ? 1 : 0;
            postUpdateItem(this);
        }

        void reverseMarked()
        {
        }
    }

    private void postUpdateItem(OrderDetItemVisual item)
    {
        itemsToUpdate.add(item);
        Handler handler = AndroidContext.mainActivity.getMainHandler();

        handler.removeCallbacks(updateItemsTask);
        handler.postDelayed(updateItemsTask, 200);
    }

    private Runnable updateItemsTask = new Runnable()
    {
        @Override
        public void run()
        {
            for(OrderDetItemVisual item : itemsToUpdate)
            {
                OrderDetailFactory.instance.update(item.orderDetail);
            }
        }
    };


    List<OrderDetItemVisual> allItems = new ArrayList<OrderDetItemVisual>();
    List<OrderDetItemVisual> usedItems = new ArrayList<OrderDetItemVisual>();

    TheListAdapter adapter;
    TheListAdapter usedAdapter;

    int normalColorBg;
    int highlightedColorBg;
    int markedColorBg;

    @AfterViews
    void initViews()
    {

        normalColorBg = Color.WHITE;
        highlightedColorBg = getResources().getColor(R.color.highlightedInList);
        markedColorBg = getResources().getColor(R.color.markedInList);

        if(order != null)
        {
//            AndroidContext.mainActivity.getSupportActionBar().setTitle(order.getComments());

            orderNumText.setText(order.getName());
            orderCommentEdit.setText(order.getComments());

            if(order.isOpen())
            {
                mode = MODE_LIST_ALL;
                locked = false;
            } else
            {
                mode = MODE_LIST_USED;
                locked = true;
            }

            setHeader();

            loadData();

            {
                listView.setHasFixedSize(true);
                final Drawable divider = getResources().getDrawable(R.drawable.divider1);
                listView.addItemDecoration(new DividerItemDecoration(divider));

                adapter = new TheListAdapter(allItems);
                listView.setAdapter(adapter);
            }
            {
                usedListView.setHasFixedSize(true);
                final Drawable divider = getResources().getDrawable(R.drawable.divider1);
                usedListView.addItemDecoration(new DividerItemDecoration(divider));

                usedAdapter = new TheListAdapter(usedItems);
                usedListView.setAdapter(usedAdapter);
            }

            setMode(mode);
        } else
        {
            AndroidContext.mainActivity.getMainHandler().post(new Runnable()
            {
                @Override
                public void run()
                {
                    AndroidContext.mainActivity.onNavigationDrawerItemSelected(FragmentFactory.FRAG_MAINTENANCE_LIST - 1);
                }
            });
        }
    }

    private void setHeader()
    {
        orderTopText.setText(Helpers.smartDateTimeString(order.createdDate));

        orderQtyText.setText(Integer.toString(order.replenishedQty));

        if(order.isOpen())
        {
            orderStatusText.setText(R.string.status_opened);
            stopBut.setImageResource(R.drawable.ic_action_stop);
        } else
        {
            orderStatusText.setText(R.string.status_done);
            stopBut.setImageResource(locked ? R.drawable.ic_action_secure
                                    : R.drawable.ic_action_not_secure);
            stopBut.setBackgroundResource(locked ? R.drawable.circle2 : R.drawable.circle4);
        }

    }

    private void addItemQty(OrderDetItemVisual item, int deltaQty)
    {
        if(locked) return;

        int oldQty = item.getQty();
        item.setQty(oldQty + deltaQty);
        if(oldQty != item.getQty())
        {
            order.replenishedQty += deltaQty;
            OrderFactory.instance.update(order);
            orderQtyText.setText(Integer.toString(order.replenishedQty));
        }
    }

    private void loadData()
    {
        allItems.clear();
        this.goodsItems = GoodsFactory.instance.getGoodsAll();
        List<OrderDetail> ordetItems = OrderDetailFactory.instance.getOrderDetails(order.getId());

        order.replenishedQty = 0;
        for(Goods goods : goodsItems)
        {
            OrderDetItemVisual item = new OrderDetItemVisual();
            item.setGoods(goods);
            for(OrderDetail orderDetail : ordetItems)
            {
                if(orderDetail.goodsId == goods.id)
                {
                    item.setOrderDetail(orderDetail);
                    order.replenishedQty += item.qty;
                    break;
                }
            }

            allItems.add(item);
        }

        OrderFactory.instance.update(order);
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        private List<OrderDetItemVisual> items;
        public TheListAdapter(List<OrderDetItemVisual> items)
        {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_goods_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder itemViewHolder, int i)
        {
            itemViewHolder.populateData(items.get(i), i);
        }

        @Override
        public int getItemCount()
        {
            return items.size();
        }

        int findPosByExtra(ViewHolder viewHolder)
        {
            for(int i=0;i<items.size();i++)
            {
                if(items.get(i).extra == viewHolder)
                    return i;
            }

            return 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            TextView nameText;
            TextView qtyText;
            ImageButton buttonPlus;
            ImageButton buttonMinus;
            ImageView okTick;
            LinearLayout layout;

            OrderDetItemVisual assignedItem;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.order_item_name);
                qtyText = (TextView) itemView.findViewById(R.id.order_item_lineqty);
                buttonMinus = (ImageButton) itemView.findViewById(R.id.order_item_minus);
                buttonPlus = (ImageButton) itemView.findViewById(R.id.order_item_plus);
                okTick = (ImageView) itemView.findViewById(R.id.order_item_ok_tick);
                layout = (LinearLayout) itemView.findViewById(R.id.order_item_main_lay);

                RepeatListenerAbstract repeater = new RepeatListenerAbstract()
                {
                    @Override
                    public void onClick(View view)
                    {
                        ViewHolder.this.onClick(view);
                    }
                };
                buttonMinus.setOnTouchListener(repeater);
                buttonPlus.setOnTouchListener(repeater);
                qtyText.setOnClickListener(this);
                nameText.setOnClickListener(this);
            }

            public void populateData(OrderDetItemVisual item, int pos)
            {
                if(assignedItem != null)
                {
                    assignedItem.extra = null;
                }

                int qty = item.getQty();
                assignedItem = item;
                item.extra = this;
                nameText.setText(item.goods.name);
                qtyText.setText(Integer.toString(qty));
                layout.setBackgroundColor(qty > 0 ? highlightedColorBg : normalColorBg);
            }

            @Override
            public void onClick(View view)
            {
                if(locked) return;

                if(view == nameText)
                {
                    int pos = findPosByExtra(this);
                    OrderDetItemVisual item = items.get(pos);
                    if(item.qty > 0)
                    {
                        item.reverseMarked();
                        notifyItemChanged(pos);
                    }
                    return;
                }

                if(view == qtyText)
                {
                    EnterTextStringDialog dialog = FragmentFactory.singleton.newTextStringDialog();
                    dialog.setTitle(R.string.enter_qty_cap);
                    dialog.setCallback(new EnterTextStringDialog.ITextCallback()
                    {
                        @Override
                        public boolean textEntered(String text)
                        {
                            try
                            {
                                int qty = Integer.parseInt(text.trim());

                                int pos = findPosByExtra(ViewHolder.this);
                                OrderDetItemVisual item = items.get(pos);

                                int delta = qty - item.getQty();
                                addItemQty(item, delta);
                                notifyItemChanged(pos);

                            } catch(Exception ex)
                            {
                                MyToast.toast(R.string.err_wrong_expression);
                                return false;
                            }
                            return true;
                        }
                    });
                    dialog.show(getFragmentManager(), "qty");
                    return;
                }

                int delta = 1;
                if(view == buttonMinus) delta = -1;

                int pos = findPosByExtra(this);
                OrderDetItemVisual item = items.get(pos);

                addItemQty(item, delta);
                notifyItemChanged(pos);
            }
        }
    }

    private void saveData()
    {
        String comments = orderCommentEdit.getText().toString().trim();
        if(comments != null && order != null
           && !comments.equals(order.getComments()))
        {
            order.setComments(comments);
            OrderFactory.instance.update(order);
        }
    }

    private void updateData()
    {
        usedItems.clear();
        for(OrderDetItemVisual item : allItems)
        {
            if(item.getQty() > 0)
            {
                usedItems.add(item);
            }
        }

        usedAdapter.notifyDataSetChanged();
    }

    private void setMode(int newMode)
    {
        mode = newMode;

        switch(mode)
        {
            case MODE_LIST_ALL:
                usedListView.setVisibility(View.GONE);
                commentLay.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
                break;
            case MODE_LIST_USED:
                usedListView.setVisibility(View.VISIBLE);
                commentLay.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                updateData();
                break;
            case MODE_COMMENTS:
                usedListView.setVisibility(View.GONE);
                commentLay.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                break;
        }
    }

    @Click(R.id.order_all_but)
    void onAllListClick()
    {
        setMode(MODE_LIST_ALL);
    }

    @Click(R.id.order_used_but)
    void onUsedListClick()
    {
        setMode(MODE_LIST_USED);
    }

    @Click(R.id.order_comment_but)
    void onCommentViewClick()
    {
        setMode(MODE_COMMENTS);
    }

    @Click(R.id.order_stop)
    void onStopClick()
    {
        if(!order.isOpen())
        {
            if(locked)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(R.string.close_cap).setMessage(R.string.unlock_order_title)
                        .setCancelable(true).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        locked = !locked;
                        setHeader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

                builder.show();
            } else
            {
                locked = !locked;
                setHeader();
            }
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.close_cap).setMessage(R.string.close_order_title)
                .setCancelable(true).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                closeOrder();
            }
        })
        .setNegativeButton(android.R.string.cancel, null);

        builder.show();
    }

    @Click(R.id.order_header_lay)
    void onHeaderClick()
    {
        if(!order.isOpen() && !locked)
        {
//            EnterMaintenanceDatesDialog dialog = FragmentFactory.singleton.newEnterMaintenanceDatesDialog();
//            dialog.setMaintenance(order);
//            dialog.setTitle(R.string.edit_maintenance_dates_title);
//            dialog.setCallback(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    setHeader();
//                }
//            });
//            dialog.show(getFragmentManager(), "edates");
            return;
        }
    }

    @Click(R.id.order_share)
    void onShareOrderClick()
    {
        updateData();
        OrderReport report = new OrderReport(order, usedItems);
        report.build();
    }

    private void closeOrder()
    {
        order.state = Order.STATE_DONE;
        OrderFactory.instance.update(order);
        locked = true;
        setHeader();
    }

    @Override
    public void onDetach()
    {
        saveData();
//        AndroidContext.mainActivity.getSupportActionBar().setTitle(R.string.title_section1);
        super.onDetach();
    }
}
