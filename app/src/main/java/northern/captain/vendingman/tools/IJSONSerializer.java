package northern.captain.vendingman.tools;

import org.json.JSONObject;

/**
 * Interface for serializing objects using JSON format
 * @author leo
 *
 */
public interface IJSONSerializer
{
	/**
	 * Serialize object into the JSONObject. Object should create a new JSONObject,
	 * put all data into it and then return this json to the caller.
	 * @param context - any context, can be null
	 */
	JSONObject  serializeJSON(IJSONContext context);
	
	/**
	 * Deserialize object from the given JSONObject. The given object is not a container,
	 * the object that really contains the data for deserialization
	 * @param object
     * @param context - any context, can be null
	 */
	void  deserializeJSON(JSONObject object, IJSONContext context);
}
