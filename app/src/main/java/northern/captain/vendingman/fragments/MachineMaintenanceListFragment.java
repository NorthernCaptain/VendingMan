package northern.captain.vendingman.fragments;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import northern.captain.vendingman.BaseFragment;
import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.Maintenance;
import northern.captain.vendingman.entities.MaintenanceFactory;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.entities.VendingMachineFactory;
import northern.captain.vendingman.tools.Helpers;

/**
 * Created by leo on 13.03.15.
 */
@EFragment(R.layout.frag_machinemainlist)
public class MachineMaintenanceListFragment extends BaseFragment
{
    @ViewById(R.id.fmachinemain_rview)
    TwoWayView listView;

    private class MachineItem
    {
        VendingMachine machine;
        boolean hasOpenMaintenance;

        public MachineItem(VendingMachine machine)
        {
            this.machine = machine;
        }

        void init()
        {
            Maintenance maintenance = MaintenanceFactory.instance.getLatestMaintenance(machine.id);
            if(maintenance != null)
            {
                hasOpenMaintenance = maintenance.isOpen();
            }
        }
    }

    List<MachineItem> items = new ArrayList<MachineItem>();

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

    private int normalBGColor;
    private int alertingBGColor;
    private int highlightTextColor;
    private int alertTextColor;

    @AfterViews
    void initViews()
    {
        updateData();

        normalBGColor = Color.WHITE;
        Resources resources = getResources();
        alertingBGColor = resources.getColor(R.color.alertedInList);
        alertTextColor = resources.getColor(R.color.alertTextColor);
        highlightTextColor = resources.getColor(R.color.highlightTextColor);;

        listView.setHasFixedSize(true);
        final Drawable divider = resources.getDrawable(R.drawable.divider1);
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
                    callback.machineChosen(items.get(pos).machine);
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
            LinearLayout mainLay;

            public ViewHolder(View itemView)
            {
                super(itemView);
                nameText = (TextView) itemView.findViewById(R.id.mmitem_name);
                descriptionText = (TextView) itemView.findViewById(R.id.mmitem_description);
                maintenanceDateText = (TextView) itemView.findViewById(R.id.mmitem_maintenance_date);
                accountingDateText = (TextView) itemView.findViewById(R.id.mmitem_accounting_date);
                mainLay = (LinearLayout) itemView.findViewById(R.id.mmitem_main_lay);
            }

            public void populateData(MachineItem item, int pos)
            {
                VendingMachine machine = item.machine;
                machine.extra = this;
                nameText.setText(machine.name);
                descriptionText.setText(machine.description);
                maintenanceDateText.setText(Helpers.smartDateString(machine.getLastMaintainDate()));
                accountingDateText.setText(Helpers.smartDateString(machine.getLastAccountDate()));
                if(item.hasOpenMaintenance)
                {
                    mainLay.setBackgroundColor(alertingBGColor);
                    maintenanceDateText.setTextColor(alertTextColor);
                } else
                {
                    mainLay.setBackgroundColor(normalBGColor);
                    maintenanceDateText.setTextColor(highlightTextColor);
                }
            }
        }
    }


    public void updateData()
    {
        List<VendingMachine> machines = VendingMachineFactory.instance.getVendingMachineAll();
        items.clear();

        for(VendingMachine machine : machines)
        {
            MachineItem item = new MachineItem(machine);
            item.init();

            items.add(item);
        }

        Collections.sort(items, new Comparator<MachineItem>()
        {
            @Override
            public int compare(MachineItem machineItem, MachineItem machineItem2)
            {
                if(!machineItem.hasOpenMaintenance && machineItem2.hasOpenMaintenance)
                {
                    return 1;
                }
                if(machineItem.hasOpenMaintenance && !machineItem2.hasOpenMaintenance)
                {
                    return -1;
                }

                return machineItem.machine.getName().compareTo(machineItem2.machine.getName());
            }
        });

        if(adapter != null)
        {
            adapter.notifyDataSetChanged();
        }
    }
}
