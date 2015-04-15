package northern.captain.vendingman;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import northern.captain.vendingman.entities.EntitiesFactory;
import northern.captain.vendingman.entities.SQLManager;
import northern.captain.vendingman.entities.VendingMachine;
import northern.captain.vendingman.fragments.MachineMaintenanceListFragment;
import northern.captain.vendingman.fragments.MachineOverviewFragment;
import northern.captain.vendingman.gui.PreferenceListFragment;
import northern.captain.vendingman.process.ColorMan;
import northern.captain.vendingman.tools.Setup_;

@EActivity
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SearchView.OnQueryTextListener,
        PreferenceListFragment.OnPreferenceAttachedListener
{
    @Pref
    public Setup_ setup;

    @StringRes(R.string.today_cap)
    public String today;

    @StringRes(R.string.yesterday_cap)
    public String yesterday;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AndroidContext.setMainActivity(this);

        setContentView(R.layout.activity_main);

        mainHandler = new Handler(getMainLooper());

        SQLManager.initialize();
//        WebProcessorA.initialize();
        EntitiesFactory.initialize();
        ColorMan.initialize();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public CharSequence getmTitle()
    {
        return mTitle;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position)
    {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, FragmentFactory.singleton.newInstance(position + 1))
                .commit();
    }

    public void openOnTop(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void onSectionAttached(int number)
    {
        switch (number)
        {
            case FragmentFactory.FRAG_MAINTENANCE_LIST:
                mTitle = getString(R.string.title_section1);
                break;
            case FragmentFactory.FRAG_GOODS_LIST:
                mTitle = getString(R.string.title_section2);
                break;
            case FragmentFactory.FRAG_MACHINE_LIST:
                mTitle = getString(R.string.title_section3);
                break;
            case FragmentFactory.FRAG_ORDERS_LIST:
                mTitle = getString(R.string.title_section4);
                break;
            case FragmentFactory.FRAG_PREFERENCES:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }

    public void restoreActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    public Menu actionMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        actionMenu = menu;
        if (!mNavigationDrawerFragment.isDrawerOpen())
        {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            openSettings();
            return true;
        }

        if (id == R.id.action_example)
        {
            doSync();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SQLManager.instance.resume();
        AndroidContext.setMainActivity(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SQLManager.instance.shutdown();
        AndroidContext.clearMainRefs();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        AndroidContext.clearMainRefs();
    }

    protected void openSettings()
    {
    }

    protected void doSync()
    {
    }

    @Override
    public boolean onQueryTextSubmit(String s)
    {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s)
    {
        return true;
    }

    @Override
    public void onPreferenceAttached(PreferenceScreen root, int xmlId)
    {

    }

    public void openMachine(VendingMachine machine, final MachineMaintenanceListFragment maintenanceListFragment)
    {
        MachineOverviewFragment fragment = FragmentFactory.singleton.newMachineOverviewFrag();
        fragment.setMachine(machine);
        fragment.setOnDetachCallback(new Runnable()
        {
            @Override
            public void run()
            {
                maintenanceListFragment.updateData();
            }
        });
        openOnTop(fragment);
    }

    public Handler getMainHandler()
    {
        return mainHandler;
    }
}
