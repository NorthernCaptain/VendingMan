package northern.captain.vendingman.fragments;

import android.os.Bundle;

import northern.captain.vendingman.R;
import northern.captain.vendingman.gui.PreferenceListFragment;

/**
 * Created by leo on 15.02.15.
 */
public class PrefFragment extends PreferenceListFragment
{
    public static final String SHARED_PREFS_NAME = "Setup";

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.preference_layout);
    }
}
