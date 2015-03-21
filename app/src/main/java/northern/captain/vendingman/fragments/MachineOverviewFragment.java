package northern.captain.vendingman.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.FragmentFactory;
import northern.captain.vendingman.MainActivity_;
import northern.captain.vendingman.R;
import northern.captain.vendingman.entities.Maintenance;
import northern.captain.vendingman.entities.MaintenanceFactory;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.entities.VendingMachineFactory;
import northern.captain.vendingman.tools.Helpers;
import northern.captain.vendingman.tools.MyToast;

/**
 * Created by leo on 3/18/15.
 */
@EFragment(R.layout.frag_machineoverview)
public class MachineOverviewFragment extends Fragment
{
    private VendingMachine machine;
    private Maintenance lastMaintenance;

    @ViewById(R.id.machinecard_name)
    TextView machineNameText;

    @ViewById(R.id.machinecard_description)
    TextView machineDescription;

    @ViewById(R.id.machinecard_main_top)
    TextView maintTopText;

    @ViewById(R.id.machinecard_comment)
    TextView maintCommentText;

    @ViewById(R.id.machinecard_status)
    TextView maintStatusText;

    @ViewById(R.id.machinecard_qty)
    TextView maintQtyText;

    @ViewById(R.id.machinecard_duration)
    TextView maintDurationText;

    public VendingMachine getMachine()
    {
        return machine;
    }

    public void setMachine(VendingMachine machine)
    {
        this.machine = machine;
    }

    @AfterViews
    void initViews()
    {
        if(machine == null) return;

        lastMaintenance = MaintenanceFactory.instance.getLatestMaintenance(machine.id);

        machineNameText.setText(machine.getName());
        machineDescription.setText(machine.getDescription());

        if(lastMaintenance != null)
        {
            StringBuilder buf = new StringBuilder(Helpers.smartDateTimeString(lastMaintenance.startDate));
            buf.append(" - ");
            Date endDate;
            if (lastMaintenance.finishDate == null)
            {
                buf.append(getResources().getString(R.string.now_label));
                endDate = new Date();
            } else
            {
                endDate = lastMaintenance.finishDate;
                buf.append(Helpers.smartDateTimeString(lastMaintenance.finishDate));
            }
            maintTopText.setText(buf.toString());

            maintDurationText.setText(
                    Helpers.deltaHoursMins(endDate.getTime() - lastMaintenance.startDate.getTime()));

            if (lastMaintenance.comments == null)
            {
                maintCommentText.setVisibility(View.GONE);
            } else
            {
                maintCommentText.setText(lastMaintenance.comments);
            }

            maintStatusText.setText(lastMaintenance.status.equals(Maintenance.STATUS_OPEN)
                    ? R.string.status_opened : R.string.status_done);

            maintQtyText.setText(Integer.toString(lastMaintenance.replenishedQty));
        }
    }

    @Click(R.id.machinecard_new_main)
    void onNewMaintenanceClick()
    {
        if(lastMaintenance != null && lastMaintenance.getStatus().equals(Maintenance.STATUS_OPEN))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.close_cap).setMessage(R.string.close_maintenance_title)
                    .setCancelable(true).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    MaintenanceFactory.instance.close(lastMaintenance);
                    doCreateMaintenance();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();

            return;
        }

        doCreateMaintenance();
    }

    private void doCreateMaintenance()
    {
        lastMaintenance = createNewMaintenance();
        initViews();
        onMaintenanceClick();
    }


    @Click(R.id.machinecard_maint_lay)
    void onMaintenanceClick()
    {
        MaintenanceFragment fragment = FragmentFactory.singleton.newMaintenanceFragment();
        fragment.setMaintenance(lastMaintenance);
        fragment.setOnDetachCallback(new Runnable()
        {
            @Override
            public void run()
            {
                initViews();
            }
        });
        AndroidContext.mainActivity.openOnTop(fragment);
    }

    @Click(R.id.machinecard_view_main_list)
    void onMaintenanceListClick()
    {
        MaintenanceListFragment fragment = FragmentFactory.singleton.newMaintenanceListFragment();
        fragment.setMachine(machine);
        fragment.setOnDetachCallback(new Runnable()
        {
            @Override
            public void run()
            {
                initViews();
            }
        });
        AndroidContext.mainActivity.openOnTop(fragment);
    }

    private Maintenance createNewMaintenance()
    {
        Maintenance maintenance = MaintenanceFactory.instance.newItem();

        maintenance.startDate = new Date();
        maintenance.status = Maintenance.STATUS_OPEN;
        maintenance.machineId = machine.id;

        MaintenanceFactory.instance.insert(maintenance);

        machine.setLastMaintainDate(maintenance.startDate);
        VendingMachineFactory.instance.update(machine);

        return maintenance;
    }
}
