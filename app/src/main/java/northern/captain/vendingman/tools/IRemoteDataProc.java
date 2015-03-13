package northern.captain.vendingman.tools;

import org.json.JSONObject;

/**
 * Interface for passing data from/to remote client
 * @author leo
 *
 */
public interface IRemoteDataProc
{
	boolean receivedFromRemote(JSONObject data);
	void sendToRemote(JSONObject data);
	void setSender(IRemoteDataProc sender);
	void connectError(Object err);
	void connected();
	void communicationError(Exception ex, String err);
	void information(String infoMessage);

    /**
     * Process before sending to main in the separate (non-gui) thread
     * @param data
     * @return
     */
    boolean processBeforeSendingToMain(JSONObject data);
}
