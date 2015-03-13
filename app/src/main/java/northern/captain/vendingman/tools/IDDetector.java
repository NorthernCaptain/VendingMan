package northern.captain.vendingman.tools;

import java.security.NoSuchAlgorithmException;

import northern.captain.vendingman.AndroidContext;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class IDDetector 
{
	public class ID
	{
		/**
		 * Android ID
		 */
		public long   aid;
		
		/**
		 * Device ID taken from IMEI
		 */
		public long   did;
		/**
		 * Wifi ID taken from WIFI module if possible
		 */
		public long   wid;
	}

	public IDDetector.ID id = new ID();
	
	public IDDetector.ID detectID()
	{
		
		String aStr = Secure.getString(AndroidContext.mainActivity.getContentResolver(), Secure.ANDROID_ID);
		
		if(aStr != null)
		{
			String s1 = aStr.substring(0, 8);
			String s2 = aStr.substring(8);
			
			long hash1 = Long.parseLong(s1, 16);
			long hash2 = Long.parseLong(s2, 16);
			
			id.aid = (hash1 << 32) & 0xffffffff00000000L;
			id.aid |= (hash2 & 0xffffffffL);
		}		
		id.did = getTelID();
		id.wid = getWifiID();
		
		return id;
	}
	
	private String reverseString(String src)
	{
		StringBuilder bld = new StringBuilder();
		
		for(int i=src.length()-1;i>=0;i--)
			bld.append(src.charAt(i));
		return bld.toString();
	}
	
	private long getTelID()
	{
		try
		{
			TelephonyManager mgr = (TelephonyManager) AndroidContext.mainActivity.getBaseContext()
									.getSystemService(Context.TELEPHONY_SERVICE);
			
			if(mgr == null)
				throw new NoSuchAlgorithmException();
			
			String devIdString = mgr.getDeviceId();
			
			if(devIdString == null || devIdString.length() == 0)
				return 0L;
			long hash1 = devIdString.hashCode();
			long hash2 = reverseString(devIdString).hashCode();
			
			hash1 = (hash1 << 32) & 0xffffffff00000000L;
			
			hash1 |= (hash2 & 0xffffffffL);
			return hash1; 
		}
		catch(Throwable thr)
		{
			return 0L;
		}
	}
	
	private long getWifiID()
	{
		try
		{
			WifiManager mgr = (WifiManager) AndroidContext.mainActivity.getBaseContext()
									.getSystemService(Context.WIFI_SERVICE);
			if(mgr == null)
				throw new NoSuchAlgorithmException();
			WifiInfo nfo = mgr.getConnectionInfo();
			if(nfo == null)
				return 0L;
			String mac = nfo.getMacAddress();
			if(mac == null || mac.length() == 0)
				return 0L;
			long hash1 = mac.hashCode();
			long hash2 = reverseString(mac).hashCode();
			hash1 = (hash1 << 32) & 0xffffffff00000000L;
			
			hash1 = hash1 | (hash2 & 0xffffffffL);
			return hash1; 
		}
		catch(Throwable thr)
		{
			return 0L;
		}
	}
}
