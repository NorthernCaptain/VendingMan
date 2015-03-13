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

import java.util.List;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.R;
import northern.captain.vendingman.dialogs.MachineEditDialog;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.entities.VendingMachineFactory;
import northern.captain.vendingman.gui.SwipeDismissRecyclerViewTouchListener;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 13.03.15.
 */
@EFragment(R.layout.frag_goodsview)
public class MachineListFragment extends BaseFragment
{
    @ViewById(R.id.fab_plus_fgoods)
    FloatingActionButton plusButton;

    @ViewById(R.id.fgoods_rview)
    TwoWayView listView;

    List<VendingMachine> items;

    TheListAdapter adapter;

    private static final long DELETION_DELAY = 3000;

    @AfterViews
    void initViews()
    {
        listView.setHasFixedSize(true);
        items = VendingMachineFactory.instance.getVendingMachineAll();
        final Drawable divider = getResources().getDrawable(R.drawable.divider1);
        listView.addItemDecoration(new DividerItemDecoration(divider));

        adapter = new TheListAdapter();
        listView.setAdapter(adapter);
        listView.setOnTouchListener(new SwipeDismissRecyclerViewTouchListener(listView, new SwipeDismissRecyclerViewTouchListener.DismissCallbacks()
        {
            @Override
            public boolean canDismiss(int position)
            {
                VendingMachine item = items.get(position);
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
        itemClicker.setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(RecyclerView recyclerView, View view, int position, long l)
            {
                onEditItemClicked(position);
                return true;
            }
        });
    }

    private class TheListAdapter extends RecyclerView.Adapter<TheListAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int type)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.machine_list_item, parent, false);
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
            VendingMachine item = items.get(pos);
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
            TextView tagsText;
            LinearLayout deletedLay;
            Button undoBut;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.mitem_name);
                tagsText = (TextView) itemView.findViewById(R.id.mitem_tags);
                descriptionText = (TextView) itemView.findViewById(R.id.mitem_description);
                deletedLay = (LinearLayout) itemView.findViewById(R.id.mitem_deleted_lay);
                undoBut = (Button) itemView.findViewById(R.id.mitem_undo_btn);
            }

            public void populateData(VendingMachine item, int pos)
            {
                item.extra = this;
                if(item.state == 1)
                {
                    deletedLay.setVisibility(View.GONE);
                    nameText.setText(item.name);
                    if(Helpers.isNullOrEmpty(item.tags))
                    {
                        tagsText.setVisibility(View.GONE);
                    } else
                    {
                        tagsText.setText(item.tags);
                        tagsText.setVisibility(View.VISIBLE);
                    }
                    descriptionText.setText(item.description);
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

            void doActualDeletion(final int pos, final VendingMachine delItem)
            {
                for(int i = 0;i<items.size();i++)
                {
                    VendingMachine item = items.get(i);
                    if (item.state == 0 && item.id == delItem.id)
                    {
                        remove(i);
                    }
                }
            }

            void startCountDown(final int pos, final VendingMachine item)
            {
                AndroidContext.mainActivity.mainHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doActualDeletion(pos, item);
                    }
                }, DELETION_DELAY);
            }
        }
    }

    private void remove(int idx)
    {
        VendingMachine item = items.get(idx);
        item.state = 0;
        VendingMachineFactory.instance.update(item);
        items.remove(idx);
        adapter.notifyItemRemoved(idx);
    }

    private void onEditItemClicked(final int pos)
    {
        MachineEditDialog dialog = FragmentFactory.singleton.newVendingMachineEditDialog();
        dialog.setItem(items.get(pos)).setCallback(new MachineEditDialog.VendingMachineEditCallback()
        {
            @Override
            public void editedOk(VendingMachine item)
            {
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void editCancel()
            {

            }
        });
        dialog.show(getFragmentManager(), "goodsedit");
    }

    private void updateData()
    {
        items = VendingMachineFactory.instance.getVendingMachineAll();
    }

    @Click(R.id.fab_plus_fgoods)
    protected void onNewItemClicked()
    {
        MachineEditDialog dialog = FragmentFactory.singleton.newVendingMachineEditDialog();
        dialog.setCallback(new MachineEditDialog.VendingMachineEditCallback()
        {
            @Override
            public void editedOk(VendingMachine item)
            {
                updateData();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void editCancel()
            {

            }
        });
        dialog.show(getFragmentManager(), "goodsedit");
    }
}
