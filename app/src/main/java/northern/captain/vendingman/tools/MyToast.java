package northern.captain.vendingman.tools;

import android.widget.Toast;

import northern.captain.vendingman.AndroidContext;

/**
 * Created by leo on 21.11.14.
 */
public class MyToast
{
    public static void toast(int resId)
    {
        Toast.makeText(AndroidContext.mainActivity, resId, Toast.LENGTH_SHORT).show();
    }

    public static void ltoast(int resId)
    {
        Toast.makeText(AndroidContext.mainActivity, resId, Toast.LENGTH_LONG).show();
    }
}
