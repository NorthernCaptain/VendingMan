package northern.captain.vendingman.fragments;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
@EFragment(R.layout.frag_machinemainlist)
public class MachineMaintenanceListFragment extends BaseFragment
{
    @ViewById(R.id.fmachinemain_rview)
    TwoWayView listView;

    List<VendingMachine> items;

    TheListAdapter adapter;

    public interface VendingChosenCallback
    {
        public void machineChosen(VendingMachine machine);
    }

    private VendingChosenCallback callback;

    public void setCallback(VendingChosenCallback callback)
    {
        this.callback = callback;
    }

    @AfterViews
    void initViews()
    {
        listView.setHasFixedSize(true);
        items = VendingMachineFactory.instance.getVendingMachineAll();
        final Drawable divider = getResources().getDrawable(R.drawable.divider1);
        listView.addItemDecoration(new DividerItemDecoration(divider));

        adapter = new TheListAdapter();
        listView.setAdapter(adapter);

        ItemClickSupport itemClicker = ItemClickSupport.addTo(listView);
        itemClicker.setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int pos, long l)
            {
                if(callback != null)
                {
                    callback.machineChosen(items.get(pos));
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
                    .inflate(R.layout.machinemain_list_item, parent, false);
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


        class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView descriptionText;
            TextView nameText;
            TextView maintenanceDateText;
            TextView accountingDateText;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.mmitem_name);
                descriptionText = (TextView) itemView.findViewById(R.id.mmitem_description);
                maintenanceDateText = (TextView) itemView.findViewById(R.id.mmitem_maintenance_date);
                accountingDateText = (TextView) itemView.findViewById(R.id.mmitem_accounting_date);
            }

            public void populateData(VendingMachine item, int pos)
            {
                item.extra = this;
                nameText.setText(item.name);
                descriptionText.setText(item.description);
                maintenanceDateText.setText(Helpers.smartDateString(item.getLastMaintainDate()));
                accountingDateText.setText(Helpers.smartDateString(item.getLastAccountDate()));
            }
        }
    }


    private void updateData()
    {
        items = VendingMachineFactory.instance.getVendingMachineAll();
    }
}
