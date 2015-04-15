package northern.captain.vendingman;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import northern.captain.vendingman.dialogs.AccountingDialog;
import northern.captain.vendingman.dialogs.AccountingDialog_;
import northern.captain.vendingman.dialogs.EnterMaintenanceDatesDialog;
import northern.captain.vendingman.dialogs.EnterMaintenanceDatesDialog_;
import northern.captain.vendingman.dialogs.EnterTextStringDialog;
import northern.captain.vendingman.dialogs.EnterTextStringDialog_;
import northern.captain.vendingman.dialogs.GoodsEditDialog;
import northern.captain.vendingman.dialogs.GoodsEditDialog_;
import northern.captain.vendingman.dialogs.MachineEditDialog;
import northern.captain.vendingman.dialogs.MachineEditDialog_;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.fragments.AccountingListFragment;
import northern.captain.vendingman.fragments.AccountingListFragment_;
import northern.captain.vendingman.fragments.GoodsListFragment_;
import northern.captain.vendingman.fragments.MachineListFragment_;
import northern.captain.vendingman.fragments.MachineMaintenanceListFragment;
import northern.captain.vendingman.fragments.MachineMaintenanceListFragment_;
import northern.captain.vendingman.fragments.MachineOverviewFragment;
import northern.captain.vendingman.fragments.MachineOverviewFragment_;
import northern.captain.vendingman.fragments.MaintenanceFragment;
import northern.captain.vendingman.fragments.MaintenanceFragment_;
import northern.captain.vendingman.fragments.MaintenanceListFragment;
import northern.captain.vendingman.fragments.MaintenanceListFragment_;
import northern.captain.vendingman.fragments.OrderFragment;
import northern.captain.vendingman.fragments.OrderFragment_;
import northern.captain.vendingman.fragments.OrderListFragment_;
import northern.captain.vendingman.fragments.PrefFragment;

/**
 * Created by leo on 14.11.14.
 */
public class FragmentFactory
{
    public static final int FRAG_MAINTENANCE_LIST = 1;
    public static final int FRAG_GOODS_LIST = 2;
    public static final int FRAG_MACHINE_LIST = 3;
    public static final int FRAG_ORDERS_LIST = 4;
    public static final int FRAG_PREFERENCES = 5;

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
            case FRAG_MAINTENANCE_LIST:
            {
                final MachineMaintenanceListFragment mfrag = new MachineMaintenanceListFragment_();
                mfrag.setCallback(new MachineMaintenanceListFragment.VendingChosenCallback()
                {
                    @Override
                    public void machineChosen(VendingMachine machine)
                    {
                        AndroidContext.mainActivity.openMachine(machine, mfrag);
                    }
                });
                fragment = mfrag;
            }
                break;
            case FRAG_GOODS_LIST:
                fragment = new GoodsListFragment_();
                break;
            case FRAG_MACHINE_LIST:
                fragment = new MachineListFragment_();
                break;
            case FRAG_ORDERS_LIST:
                fragment = new OrderListFragment_();
                break;
            case FRAG_PREFERENCES:
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

    public EnterTextStringDialog newTextStringDialog() { return new EnterTextStringDialog_(); }

    public MachineEditDialog newVendingMachineEditDialog()
    {
        return new MachineEditDialog_();
    }

    public MachineOverviewFragment newMachineOverviewFrag() { return new MachineOverviewFragment_();}

    public MaintenanceFragment newMaintenanceFragment() { return new MaintenanceFragment_();}

    public MaintenanceListFragment newMaintenanceListFragment() { return new MaintenanceListFragment_();}

    public OrderFragment newOrderFragment() { return  new OrderFragment_();}

    public AccountingListFragment newAccountingListFragment() { return new AccountingListFragment_();}

    public EnterMaintenanceDatesDialog newEnterMaintenanceDatesDialog() { return new EnterMaintenanceDatesDialog_();}

    public AccountingDialog newAccountingDialog() { return new AccountingDialog_();}
}
