package northern.captain.vendingman.fragments;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
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
import northern.captain.vendingman.entities.Goods;
import northern.captain.vendingman.entities.GoodsFactory;
import northern.captain.vendingman.entities.Maintenance;
import northern.captain.vendingman.entities.MaintenanceFactory;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.entities.VendingMachineFactory;
import northern.captain.vendingman.gui.SwipeDismissRecyclerViewTouchListener;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 13.03.15.
 */
@EFragment(R.layout.frag_machinemainlist)
public class MaintenanceListFragment extends BaseFragment
{
    @ViewById(R.id.fmachinemain_rview)
    TwoWayView listView;

    List<Maintenance> items;

    TheListAdapter adapter;

    VendingMachine machine;

    public interface ChosenCallback
    {
        public void itemChosen(Maintenance machine);
    }

    private ChosenCallback callback;

    public void setCallback(ChosenCallback callback)
    {
        this.callback = callback;
    }

    public void setMachine(VendingMachine machine)
    {
        this.machine = machine;
    }

    @AfterViews
    void initViews()
    {
        listView.setHasFixedSize(true);
        items = MaintenanceFactory.instance.getMaintenanceList(machine.id);
        final Drawable divider = getResources().getDrawable(R.drawable.divider1);
        listView.addItemDecoration(new DividerItemDecoration(divider));

        adapter = new TheListAdapter();
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new SwipeDismissRecyclerViewTouchListener(listView, new SwipeDismissRecyclerViewTouchListener.DismissCallbacks()
        {
            @Override
            public boolean canDismiss(int position)
            {
                Maintenance item = items.get(position);
                return item.state == 1;
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
            public void onItemClick(RecyclerView recyclerView, View view, int pos, long l)
            {
                if(callback != null)
                {
                    callback.itemChosen(items.get(pos));
                } else
                {
                    doClick(items.get(pos), pos);
                }
            }
        });
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.maint_list_item, parent, false);
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
            Maintenance item = items.get(pos);
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
            TextView maintTopText;
            TextView maintCommentText;
            TextView maintStatusText;
            TextView maintQtyText;
            TextView maintDurationText;

            LinearLayout deletedLay;
            Button undoBut;

            public ViewHolder(View itemView)
            {
                super(itemView);
                maintTopText = (TextView) itemView.findViewById(R.id.maintlist_main_top);
                maintCommentText = (TextView) itemView.findViewById(R.id.maintlist_comment);
                maintDurationText = (TextView) itemView.findViewById(R.id.maintlist_duration);
                maintStatusText = (TextView) itemView.findViewById(R.id.maintlist_status);
                maintQtyText = (TextView) itemView.findViewById(R.id.maintlist_qty);

                deletedLay = (LinearLayout) itemView.findViewById(R.id.maintlist_deleted_lay);
                undoBut = (Button) itemView.findViewById(R.id.maintlist_undo_btn);
            }

            public void populateData(Maintenance item, int pos)
            {
                item.extra = this;
                if(item.state == 1)
                {
                    deletedLay.setVisibility(View.GONE);

                    StringBuilder buf = new StringBuilder(Helpers.smartDateTimeString(item.startDate));
                    buf.append(" - ");
                    Date endDate;
                    if (item.finishDate == null)
                    {
                        buf.append(getResources().getString(R.string.now_label));
                        endDate = new Date();
                    } else
                    {
                        endDate = item.finishDate;
                        buf.append(Helpers.smartDateTimeString(item.finishDate));
                    }
                    maintTopText.setText(buf.toString());

                    maintDurationText.setText(
                            Helpers.deltaHoursMins(endDate.getTime() - item.startDate.getTime()));

                    if (item.comments == null)
                    {
                        maintCommentText.setVisibility(View.GONE);
                    } else
                    {
                        maintCommentText.setText(item.comments);
                    }

                    maintStatusText.setText(item.isOpen()
                            ? R.string.status_opened : R.string.status_done);

                    maintQtyText.setText(Integer.toString(item.replenishedQty));
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

            void doActualDeletion(final int pos, final Maintenance delItem)
            {
                for(int i = 0;i<items.size();i++)
                {
                    Maintenance item = items.get(i);
                    if (item.state == 0 && item.id == delItem.id)
                    {
                        remove(i);
                    }
                }
            }

            void startCountDown(final int pos, final Maintenance item)
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


    private void updateData()
    {
        items = MaintenanceFactory.instance.getMaintenanceList(machine.id);
        adapter.notifyDataSetChanged();
    }

    private void doClick(Maintenance item, int pos)
    {
        MaintenanceFragment fragment = FragmentFactory.singleton.newMaintenanceFragment();
        fragment.setMaintenance(item, machine);
        fragment.setOnDetachCallback(new Runnable()
        {
            @Override
            public void run()
            {
                updateData();
            }
        });
        AndroidContext.mainActivity.openOnTop(fragment);
    }

    private void remove(int idx)
    {
        Maintenance item = items.get(idx);
        item.state = 0;
        MaintenanceFactory.instance.update(item);
        items.remove(idx);
//        adapter.notifyItemRemoved(idx);
        adapter.notifyDataSetChanged();
    }

}
