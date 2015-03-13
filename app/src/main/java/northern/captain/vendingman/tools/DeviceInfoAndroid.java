package northern.captain.vendingman.tools;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import northern.captain.vendingman.AndroidContext;

public class DeviceInfoAndroid extends DeviceInfo
{
	private static volatile DeviceInfoAndroid singleton;
	
	public static DeviceInfoAndroid instance()
	{
		if(singleton == null)
			singleton = new DeviceInfoAndroid();
		return singleton;
	}
	
	public DeviceInfoAndroid()
	{
		this.board = Build.BOARD;
		this.device = Build.DEVICE;
		this.fingerprint = Build.FINGERPRINT;
		this.manufacturer = Build.MANUFACTURER;
		this.model = Build.MODEL;
		this.product = Build.PRODUCT;
		this.version = Build.VERSION.RELEASE;
		this.tags = Build.TAGS;
		
		DisplayMetrics dm = new DisplayMetrics();
		AndroidContext.mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		this.dispDPI = dm.densityDpi;
		this.dispHeight = dm.heightPixels;
		this.dispWidth = dm.widthPixels;
		this.dispScaleF = dm.scaledDensity;
		this.dispXDPI = Math.round(dm.xdpi);
		this.dispYDPI = Math.round(dm.ydpi);
		Configuration cfg = AndroidContext.mainActivity.getResources().getConfiguration();
		
		this.screenSize = "XL";
		int lay = cfg.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if(lay == Configuration.SCREENLAYOUT_SIZE_SMALL)
			this.screenSize = "S";
		if(lay == Configuration.SCREENLAYOUT_SIZE_NORMAL)
			this.screenSize = "N";
		if(lay == Configuration.SCREENLAYOUT_SIZE_LARGE)
			this.screenSize = "L";
	}
	
    @Override
    public String getNetworkInfo()
    {
        ConnectivityManager man = (ConnectivityManager) AndroidContext.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(man == null)
            return UNKNOWN;

        NetworkInfo nfo = man.getActiveNetworkInfo();
        if(nfo == null)
            return UNKNOWN;

        return nfo.getTypeName() + ":" + nfo.getType() + ":" + nfo.getSubtypeName();
    }
	
}
