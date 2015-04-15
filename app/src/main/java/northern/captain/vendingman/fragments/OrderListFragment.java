package northern.captain.vendingman.fragments;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.Date;
import java.util.List;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.R;
import northern.captain.vendingman.dialogs.GoodsEditDialog;
import northern.captain.vendingman.entities.Goods;
import northern.captain.vendingman.entities.GoodsFactory;
import northern.captain.vendingman.entities.Order;
import northern.captain.vendingman.entities.OrderFactory;
import northern.captain.vendingman.gui.SwipeDismissRecyclerViewTouchListener;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 13.03.15.
 */
@EFragment(R.layout.frag_goodsview)
public class OrderListFragment extends BaseFragment
{
    @ViewById(R.id.fab_plus_fgoods)
    FloatingActionButton plusButton;

    @ViewById(R.id.fgoods_rview)
    TwoWayView listView;

    List<Order> items;

    TheListAdapter adapter;

    @AfterViews
    void initViews()
    {
        listView.setHasFixedSize(true);
        items = OrderFactory.instance.getOrderList();
        final Drawable divider = getResources().getDrawable(R.drawable.divider1);
        listView.addItemDecoration(new DividerItemDecoration(divider));

        adapter = new TheListAdapter();
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new SwipeDismissRecyclerViewTouchListener(listView, new SwipeDismissRecyclerViewTouchListener.DismissCallbacks()
        {
            @Override
            public boolean canDismiss(int position)
            {
                Order item = items.get(position);
                return item.state != Order.STATE_DELETED;
            }

            @Override
            public void onDismiss(RecyclerView recyclerView, List<SwipeDismissRecyclerViewTouchListener.PendingDismissData> pendingDismissData)
            {
            }

            @Override
            public boolean showUndo(SwipeDismissRecyclerViewTouchListener.PendingDismissData pendingDismissData)
            {
                adapter.showUndo(pendingDismissData.position, pendingDismissData.view);
                return true;
            }
        }));

        ItemClickSupport itemClicker = ItemClickSupport.addTo(listView);
        itemClicker.setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position, long l)
            {
                onEditItemClicked(position);
            }
        });
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_list_item, parent, false);
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

        public void showUndo(int pos, View view)
        {
            Order item = items.get(pos);
            item.state = 0;
            if(item.extra instanceof ViewHolder)
            {
                ViewHolder holder = (ViewHolder)item.extra;
                holder.showUndo(pos);
                holder.startCountDown(pos, item);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView descriptionText;
            TextView nameText;
            TextView dateText;
            TextView statusText;
            LinearLayout deletedLay;
            Button undoBut;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.orditem_name);
                dateText = (TextView) itemView.findViewById(R.id.orditem_date);
                descriptionText = (TextView) itemView.findViewById(R.id.orditem_description);
                statusText = (TextView) itemView.findViewById(R.id.orditem_status);
                deletedLay = (LinearLayout) itemView.findViewById(R.id.orditem_deleted_lay);
                undoBut = (Button) itemView.findViewById(R.id.orditem_undo_btn);
            }

            public void populateData(Order item, int pos)
            {
                item.extra = this;
                if(item.state != Order.STATE_DELETED)
                {
                    deletedLay.setVisibility(View.GONE);
                    nameText.setText(getResources().getString(R.string.order_num) + item.id);
                    if(Helpers.isNullOrEmpty(item.comments))
                    {
                        descriptionText.setVisibility(View.GONE);
                    } else
                    {
                        descriptionText.setText(item.comments);
                        descriptionText.setVisibility(View.VISIBLE);
                    }
                    statusText.setText(item.state == Order.STATE_OK ? R.string.status_opened : R.string.status_done);
                } else
                {
                    showUndo(pos);
                }
            }

            public void showUndo(final int pos)
            {
                deletedLay.setVisibility(View.VISIBLE);
                undoBut.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        undoDeletion(pos);
                    }
                });
            }

            void undoDeletion(final int pos)
            {
                items.get(pos).state = 1;
                deletedLay.setVisibility(View.GONE);
                notifyItemChanged(pos);
            }

            void doActualDeletion(final int pos, final Order delItem)
            {
                for(int i = 0;i<items.size();i++)
                {
                    Order item = items.get(i);
                    if (item.state == 0 && item.id == delItem.id)
                    {
                        remove(i);
                    }
                }
            }

            void startCountDown(final int pos, final Order item)
            {
                AndroidContext.mainActivity.mainHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doActualDeletion(pos, item);
                    }
                }, Helpers.DELETION_DELAY);
            }
        }
    }

    private void remove(int idx)
    {
        Order item = items.get(idx);
        item.state = Order.STATE_DELETED;
        OrderFactory.instance.update(item);
        items.remove(idx);
        adapter.notifyDataSetChanged();
    }

    private void onEditItemClicked(final int pos)
    {
        openOrder(items.get(pos));
    }

    private void updateData()
    {
        items = OrderFactory.instance.getOrderList();
    }

    private void openOrder(Order order)
    {
        OrderFragment fragment = FragmentFactory.singleton.newOrderFragment();
        fragment.setOrder(order);
        fragment.setOnDetachCallback(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.notifyDataSetChanged();
            }
        });

        AndroidContext.mainActivity.openOnTop(fragment);
    }

    @Click(R.id.fab_plus_fgoods)
    protected void onNewItemClicked()
    {
        Order newOrder = OrderFactory.instance.newItem();
        OrderFactory.instance.insert(newOrder);
        updateData();
        adapter.notifyDataSetChanged();

        openOrder(newOrder);
    }
}
