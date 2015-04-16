package northern.captain.vendingman.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.R;
import northern.captain.vendingman.dialogs.EnterMaintenanceDatesDialog;
import northern.captain.vendingman.dialogs.EnterTextStringDialog;
import northern.captain.vendingman.entities.Goods;
import northern.captain.vendingman.entities.GoodsFactory;
import northern.captain.vendingman.entities.Maintenance;
import northern.captain.vendingman.entities.MaintenanceFactory;
import northern.captain.vendingman.entities.Order;
import northern.captain.vendingman.entities.OrderDetail;
import northern.captain.vendingman.entities.OrderDetailFactory;
import northern.captain.vendingman.entities.OrderFactory;
import northern.captain.vendingman.entities.Replenishment;
import northern.captain.vendingman.entities.ReplenishmentFactory;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.gui.RepeatListenerAbstract;
import northern.captain.vendingman.tools.Helpers;
import northern.captain.vendingman.tools.MyToast;

/**
 * Created by leo on 3/20/15.
 */
@EFragment(R.layout.frag_maintenance)
public class MaintenanceFragment extends BaseFragment
{
    @ViewById(R.id.maint_dates_lb)
    TextView maintTopText;

    @ViewById(R.id.maint_comment_edit)
    EditText maintCommentEdit;

    @ViewById(R.id.maint_status_lb)
    TextView maintStatusText;

    @ViewById(R.id.maint_repl_qty_lb)
    TextView maintQtyText;

    @ViewById(R.id.maint_all_rview)
    TwoWayView listView;

    @ViewById(R.id.maint_selected_rview)
    TwoWayView usedListView;

    @ViewById(R.id.maint_comment_lay)
    View commentLay;

    @ViewById(R.id.maint_stop)
    ImageButton stopBut;

    private boolean locked = false;

    private static final int MODE_LIST_ALL = 1;
    private static final int MODE_LIST_USED = 2;
    private static final int MODE_COMMENTS = 3;

    private int mode = MODE_LIST_ALL;

    private Maintenance maintenance;
    private VendingMachine machine;

    public void setMaintenance(Maintenance maintenance, VendingMachine machine)
    {
        this.maintenance = maintenance;
        this.machine = machine;
    }

    List<Goods> goodsItems;

    Set<ReplItem> itemsToUpdate = new HashSet<ReplItem>();

    private class ReplItem
    {
        Goods goods;
        Replenishment replenishment;
        int qty;
        Object extra;

        void setReplenishment(Replenishment repl)
        {
            replenishment = repl;
            qty = repl == null ? 0 : replenishment.qty;
        }

        void setGoods(Goods goods)
        {
            this.goods = goods;
        }

        int getQty() { return qty;}
        void setQty(int newQty)
        {
            qty = newQty < 0 ? 0 : newQty;

            if(replenishment == null)
            {
                replenishment = ReplenishmentFactory.instance.newItem();
                replenishment.mainId = maintenance.id;
                replenishment.goodsId = goods.id;
            }

            replenishment.setQty(qty);
            replenishment.state = qty > 0 ? 1 : 0;
            replenishment.setMarked(qty > 0 ? replenishment.isMarked() : false);
            postUpdateItem(this);
        }

        void reverseMarked()
        {
            replenishment.setMarked(!replenishment.isMarked());
            ReplenishmentFactory.instance.update(replenishment);
        }
    }

    private void postUpdateItem(ReplItem item)
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
            for(ReplItem item : itemsToUpdate)
            {
                ReplenishmentFactory.instance.update(item.replenishment);
            }
        }
    };


    List<ReplItem> allItems = new ArrayList<ReplItem>();
    List<ReplItem> usedItems = new ArrayList<ReplItem>();

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

        if(maintenance != null)
        {
            AndroidContext.mainActivity.getSupportActionBar().setTitle(machine.getName());

            maintCommentEdit.setText(maintenance.getComments());

            if(maintenance.isOpen())
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
        StringBuilder buf = new StringBuilder(Helpers.smartDateTimeString(maintenance.startDate));
        buf.append(" - ");
        Date endDate;
        if (maintenance.finishDate == null)
        {
            buf.append(getResources().getString(R.string.now_label));
            endDate = new Date();
        } else
        {
            endDate = maintenance.finishDate;
            buf.append(Helpers.smartDateTimeString(maintenance.finishDate));
        }
        buf.append(" [");
        buf.append(Helpers.deltaHoursMins(endDate.getTime() - maintenance.startDate.getTime()));
        buf.append("]");

        maintTopText.setText(buf.toString());

        maintQtyText.setText(Integer.toString(maintenance.replenishedQty));

        if(maintenance.isOpen())
        {
            maintStatusText.setText(R.string.status_opened);
            stopBut.setImageResource(R.drawable.ic_action_stop);
        } else
        {
            maintStatusText.setText(R.string.status_done);
            stopBut.setImageResource(locked ? R.drawable.ic_action_secure
                                    : R.drawable.ic_action_not_secure);
            stopBut.setBackgroundResource(locked ? R.drawable.circle2 : R.drawable.circle1);
        }

    }

    private void addItemQty(ReplItem item, int deltaQty)
    {
        if(locked) return;

        int oldQty = item.getQty();
        item.setQty(oldQty + deltaQty);
        if(oldQty != item.getQty())
        {
            maintenance.replenishedQty += deltaQty;
            MaintenanceFactory.instance.update(maintenance);
            maintQtyText.setText(Integer.toString(maintenance.replenishedQty));
        }
    }

    private void loadData()
    {
        allItems.clear();
        this.goodsItems = GoodsFactory.instance.getGoodsAll();
        List<Replenishment> replItems = ReplenishmentFactory.instance.getReplenishments(maintenance.getId());

        maintenance.replenishedQty = 0;
        for(Goods goods : goodsItems)
        {
            ReplItem item = new ReplItem();
            item.setGoods(goods);
            for(Replenishment replenishment : replItems)
            {
                if(replenishment.goodsId == goods.id)
                {
                    item.setReplenishment(replenishment);
                    maintenance.replenishedQty += item.qty;
                    break;
                }
            }

            allItems.add(item);
        }

        MaintenanceFactory.instance.update(maintenance);
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        private List<ReplItem> items;
        public TheListAdapter(List<ReplItem> items)
        {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.maint_goods_list_item, parent, false);
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

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
        {
            TextView nameText;
            TextView qtyText;
            ImageButton buttonPlus;
            ImageButton buttonMinus;
            ImageView okTick;
            LinearLayout layout;

            ReplItem assignedItem;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.maint_gitem_name);
                qtyText = (TextView) itemView.findViewById(R.id.maint_gitem_lineqty);
                buttonMinus = (ImageButton) itemView.findViewById(R.id.maint_gitem_minus);
                buttonPlus = (ImageButton) itemView.findViewById(R.id.maint_gitem_plus);
                okTick = (ImageView) itemView.findViewById(R.id.maint_gitem_ok_tick);
                layout = (LinearLayout) itemView.findViewById(R.id.maint_gitem_main_lay);

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
                nameText.setOnLongClickListener(this);
            }

            public void populateData(ReplItem item, int pos)
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
                if(item.replenishment != null && item.replenishment.isMarked())
                {
                    layout.setBackgroundColor(markedColorBg);
                    okTick.setVisibility(View.VISIBLE);
                } else
                {
                    layout.setBackgroundColor(qty > 0 ? highlightedColorBg : normalColorBg);
                    okTick.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onClick(View view)
            {
                if(locked) return;

                if(view == nameText)
                {
                    int pos = findPosByExtra(this);
                    ReplItem item = items.get(pos);
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
                                ReplItem item = items.get(pos);

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
                ReplItem item = items.get(pos);

                addItemQty(item, delta);
                notifyItemChanged(pos);
            }

            @Override
            public boolean onLongClick(View view)
            {
                if(locked) return false;

                int pos = findPosByExtra(this);
                ReplItem item = items.get(pos);

                askForOrder(item.goods);
                return true;
            }
        }
    }

    private void saveData()
    {
        String comments = maintCommentEdit.getText().toString().trim();
        if(comments != null && maintenance!= null
           && !comments.equals(maintenance.getComments()))
        {
            maintenance.setComments(comments);
            MaintenanceFactory.instance.update(maintenance);
        }
    }

    private void updateData()
    {
        usedItems.clear();
        for(ReplItem  item : allItems)
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

    @Click(R.id.maint_all_but)
    void onAllListClick()
    {
        setMode(MODE_LIST_ALL);
    }

    @Click(R.id.maint_used_but)
    void onUsedListClick()
    {
        setMode(MODE_LIST_USED);
    }

    @Click(R.id.maint_comment_but)
    void onCommentViewClick()
    {
        setMode(MODE_COMMENTS);
    }

    @Click(R.id.maint_stop)
    void onStopClick()
    {
        if(!maintenance.isOpen())
        {
            if(locked)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(R.string.close_cap).setMessage(R.string.unlock_maintenance_title)
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

        builder.setTitle(R.string.close_cap).setMessage(R.string.close_maintenance_title)
                .setCancelable(true).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                closeMaintenance();
            }
        })
        .setNegativeButton(android.R.string.cancel, null);

        builder.show();
    }

    @Click(R.id.maint_header_lay)
    void onHeaderClick()
    {
        if(!maintenance.isOpen() && !locked)
        {
            EnterMaintenanceDatesDialog dialog = FragmentFactory.singleton.newEnterMaintenanceDatesDialog();
            dialog.setMaintenance(maintenance);
            dialog.setTitle(R.string.edit_maintenance_dates_title);
            dialog.setCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    setHeader();
                }
            });
            dialog.show(getFragmentManager(), "edates");
            return;
        }
    }

    @Click(R.id.maint_order_but)
    void onOrderClick()
    {
        Order order = OrderFactory.instance.getLatestOrder();
        if(order == null)
        {
            order = OrderFactory.instance.newItem();
            OrderFactory.instance.insert(order);
        }

        OrderFragment fragment = FragmentFactory.singleton.newOrderFragment();
        fragment.setOrder(order);
        AndroidContext.mainActivity.openOnTop(fragment);
    }

    private void closeMaintenance()
    {
        MaintenanceFactory.instance.close(maintenance);
        locked = true;
        setHeader();
    }

    @Override
    public void onDetach()
    {
        saveData();
        AndroidContext.mainActivity.getSupportActionBar().setTitle(R.string.title_section1);
        super.onDetach();
    }

    private void askForOrder(final Goods goods)
    {
        EnterTextStringDialog dialog = FragmentFactory.singleton.newTextStringDialog();
        dialog.setTitle(R.string.enter_order_qty_cap);
        dialog.setCallback(new EnterTextStringDialog.ITextCallback()
        {
            @Override
            public boolean textEntered(String text)
            {
                try
                {
                    int qty = Integer.parseInt(text.trim());
                    addOrderLineQty(goods, qty);
                } catch(Exception ex)
                {
                    MyToast.toast(R.string.err_wrong_expression);
                    return false;
                }
                return true;
            }
        });
        dialog.show(getFragmentManager(), "qty");
    }

    private void addOrderLineQty(Goods goods, int qty)
    {
        Order order = OrderFactory.instance.getLatestOrder();
        if(order == null)
        {
            order = OrderFactory.instance.newItem();
            OrderFactory.instance.insert(order);
        }

        List<OrderDetail> details = OrderDetailFactory.instance.getOrderDetails(order.id);

        OrderDetail ourDetail = null;
        for(OrderDetail detail : details)
        {
            if(detail.goodsId == goods.id)
            {
                ourDetail = detail;
                break;
            }
        }

        if(ourDetail == null) ourDetail = OrderDetailFactory.instance.newItem();

        ourDetail.setQty(qty);
        ourDetail.setOrderId(order.id);
        ourDetail.setGoodsId(goods.id);
        OrderDetailFactory.instance.update(ourDetail);
    }
}
