package northern.captain.vendingman.tools;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by leo on 20.11.14.
 */
@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface Setup
{
    int defaultCategoryId();
    int defaultWalletId();
    int deviceId();

    @DefaultString(value = "me")
    String userName();

    @DefaultBoolean(value = false)
    boolean newClientOnEveryRequest();

    @DefaultBoolean(value = true)
    boolean useServerSync();

    @DefaultInt(value = 0)
    int syncGroupId();

    @DefaultString(value = "1234")
    String syncGroupPin();

    @DefaultInt(value = 0)
    int lastSyncId();

    @DefaultInt(value = 1)
    int lastScreenIndex();
}
