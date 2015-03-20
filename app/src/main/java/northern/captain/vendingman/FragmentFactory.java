package northern.captain.vendingman;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import northern.captain.vendingman.dialogs.GoodsEditDialog;
import northern.captain.vendingman.dialogs.GoodsEditDialog_;
import northern.captain.vendingman.dialogs.MachineEditDialog;
import northern.captain.vendingman.dialogs.MachineEditDialog_;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.fragments.GoodsListFragment_;
import northern.captain.vendingman.fragments.MachineListFragment_;
import northern.captain.vendingman.fragments.MachineMaintenanceListFragment;
import northern.captain.vendingman.fragments.MachineMaintenanceListFragment_;
import northern.captain.vendingman.fragments.MachineOverviewFragment;
import northern.captain.vendingman.fragments.MachineOverviewFragment_;
import northern.captain.vendingman.fragments.MaintenanceFragment;
import northern.captain.vendingman.fragments.MaintenanceFragment_;
import northern.captain.vendingman.fragments.PrefFragment;

/**
 * Created by leo on 14.11.14.
 */
public class FragmentFactory
{
    public static FragmentFactory singleton = new FragmentFactory();
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public Fragment newInstance(int sectionNumber)
    {
        Fragment fragment;
        switch(sectionNumber)
        {
            case 1:
            {
                MachineMaintenanceListFragment mfrag = new MachineMaintenanceListFragment_();
                mfrag.setCallback(new MachineMaintenanceListFragment.VendingChosenCallback()
                {
                    @Override
                    public void machineChosen(VendingMachine machine)
                    {
                        AndroidContext.mainActivity.openMachine(machine);
                    }
                });
                fragment = mfrag;
            }
                break;
            case 2:
                fragment = new GoodsListFragment_();
                break;
            case 3:
                fragment = new MachineListFragment_();
                break;
            case 4:
                fragment = new PrefFragment();
                break;
            default:
                fragment = new BaseFragment_();
                break;
        }
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        AndroidContext.mainActivity.setup.lastScreenIndex().put(sectionNumber-1);
        return fragment;
    }

    public GoodsEditDialog newGoodsEditDialog()
    {
        return new GoodsEditDialog_();
    }

    public MachineEditDialog newVendingMachineEditDialog()
    {
        return new MachineEditDialog_();
    }

    public MachineOverviewFragment newMachineOverviewFrag() { return new MachineOverviewFragment_();}

    public MaintenanceFragment newMaintenanceFragment() { return new MaintenanceFragment_();}
}
