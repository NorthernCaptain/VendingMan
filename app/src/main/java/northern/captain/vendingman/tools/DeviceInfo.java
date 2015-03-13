package northern.captain.vendingman.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceInfo implements IJSONSerializer
{
	public String board;
	public String device;
	public String manufacturer;
	public String fingerprint;
	public String model;
	public String product;
	public String version;
	public String tags;
	
	public int dispWidth;
	public int dispHeight;
	public int dispDPI;
	public float dispYDPI;
	public float dispXDPI;
	public float dispScaleF;
	public String screenSize;

    public IDDetector idDetector = new IDDetector();

	@Override
	public JSONObject serializeJSON(IJSONContext context)
	{
		JSONObject json = new JSONObject();
		try
		{
			json.put("type", "devi");
			json.put("brd", board);
			json.put("dev", device);
			json.put("man", manufacturer);
			json.put("fng", fingerprint);
			json.put("mod", model);
			json.put("prd", product);
			json.put("avr", version);
			json.put("tag", tags);
			
			json.put("dwid", dispWidth);
			json.put("dhei", dispHeight);
			json.put("ddpi", dispDPI);
			json.put("dxdp", dispXDPI);
			json.put("dydp", dispYDPI);
			json.put("dscl", dispScaleF);
			json.put("dssz", screenSize);

            JSONArray jar = new JSONArray();
            IDDetector.ID id = idDetector.detectID();

            jar.put(id.aid);
            jar.put(id.did);
            jar.put(id.wid);

            json.put("iii", jar);
            json.put("ni", getNetworkInfo());
		}
		catch(JSONException jex)
		{
			
		}
		return json;
	}

	@Override
	public void deserializeJSON(JSONObject json, IJSONContext context)
	{
		try
		{
			if(!"devi".equals(json.getString("type")))
				return;
			
			board = json.getString("brd");
			device = json.getString("dev");
			manufacturer = json.getString("man");
			fingerprint = json.getString("fng");
			model = json.getString("mod");
			product = json.getString("prd");
			version = json.getString("avr");
			tags = json.getString("tag");
			
			dispWidth = json.getInt("dwid");
			dispHeight = json.getInt("dhei");
			dispDPI = json.getInt("ddpi");
			dispXDPI = (float)json.getDouble("dxdp");
			dispYDPI = (float)json.getDouble("dydp");
			dispScaleF = (float)json.getDouble("dscl");
			screenSize = json.getString("dssz");
		}
		catch(JSONException jex)
		{
			
		}
	}

    protected static final String UNKNOWN = "unk";
    public String getNetworkInfo()
    {
        return UNKNOWN;
    }
}
