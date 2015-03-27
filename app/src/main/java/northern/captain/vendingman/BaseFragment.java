package northern.captain.vendingman;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.androidannotations.annotations.EFragment;


/**
 * A placeholder fragment containing a simple view.
 */
@EFragment(R.layout.fragment_main)
public class BaseFragment extends Fragment
{
    public BaseFragment()
    {
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        MainActivity mainActivity = (MainActivity) activity;
        Bundle bundle = getArguments();
        if(bundle != null)
        {
            mainActivity.onSectionAttached(
                    bundle.getInt(FragmentFactory.ARG_SECTION_NUMBER));
        }
    }

    public Runnable onDetachCallback;

    public void setOnDetachCallback(Runnable onDetachCallback)
    {
        this.onDetachCallback = onDetachCallback;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if(onDetachCallback != null)
        {
            onDetachCallback.run();
        }
    }
}
