package northern.captain.vendingman.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.HandlerThread;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import northern.captain.vendingman.AndroidContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class organizes communication with our web server
 * @author Leo
 *
 */
public class WebProcessorA extends ExtComProcessor
{
	protected final static String tag = "xpenseweb";

//	protected final static String mainURL = "http://10.19.76.2:8080/XPensesEnterprise-war/api/v1/";
	protected final static String mainURL = "http://api.navalclash.com/XPensesEnterprise-war/api/v1/";
	//protected final static String mainURL = "http://192.168.9.3:8080/naval/clash/api/";

	protected HttpClientHolder  client;

	private static WebProcessorA singleton = null;

	public static boolean newClientOnEveryRequest = false;

	public static class HttpClientHolder
	{
		public AsyncHttpClient client;
		public HttpClientHolder(AsyncHttpClient cln)
		{
			client = cln;
		}
	}

	public static WebProcessorA instance()
	{
		if(singleton == null)
		{
            AsyncHttpClient.allowRetryExceptionClass(InterruptedIOException.class);
            AsyncHttpClient.allowRetryExceptionClass(HttpHostConnectException.class);

            singleton = new WebProcessorA();
			singleton.startTransfer();
		}
		return singleton;
	}

    public static void initialize()
    {
        final ConditionVariable started = new ConditionVariable();

        android.os.HandlerThread handlerThread = new HandlerThread("loop")
        {
            @Override
            protected void onLooperPrepared()
            {
                super.onLooperPrepared();
                Log.i("xpenseweb", "Command thread started: " + Thread.currentThread().getName());
                WebProcessorA.instance();
                started.open();
            }
        };

        handlerThread.start();
        started.block(6000);
    }


	public void clearInstance()
	{
		if(singleton == null)
			return;
		singleton.setClientCallback(null);
		singleton.shutdown();
		singleton = null;
	}

	public WebProcessorA()
	{
		super();
		maxSendTries = 3;

		client = new HttpClientHolder(newHttpClient());
        asyncHttpResponseHandler = new AsyncHandlerWeb(MSG_READ_DATA, MSG_WRITER_DIED);
	}

	protected static void setRequestMode()
	{
//		if(SeabattleNCore.instance().locale != null && SeabattleNCore.instance().locale.startsWith("cs"))
//		{
//			newClientOnEveryRequest = true; //hack for Tomas :(
//		}

		newClientOnEveryRequest = AndroidContext.mainActivity.setup.newClientOnEveryRequest().get();
	}


	protected AsyncHttpClient newHttpClient()
	{
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setTimeout(30000);
        asyncHttpClient.setMaxRetriesAndTimeout(3, 1000);

        setHttpHeader(asyncHttpClient);

        return asyncHttpClient;
	}

	protected AsyncHttpClient newHttpClient(AsyncHttpClient oldClient)
	{
		if(oldClient != null)
        {
            oldClient.cancelRequests(null, true);
        }


		return newHttpClient();
	}

    @Override
    public Handler getMainHandler()
    {
        if(mainHandler == null)
        {
            mainHandler = new Handler(Looper.getMainLooper(),
                    new Handler.Callback()
                    {
                        @Override
                        public boolean handleMessage(android.os.Message message)
                        {
                            processMainMessage(message);
                            resetWriteHandle();
                            pumpWritingQueue();
                            return true;
                        }
                    });
        }

        return mainHandler;
    }

    protected List<Message> writingQue = new LinkedList<Message>();
    protected RequestHandle writingHandle = null;


    protected synchronized void resetWriteHandle()
    {
        writingHandle = null;
    }


    protected void writeBackToWritingQueue(JSONObject json)
    {
        if(json == null)
            return;
        Message msg = writingThread.obtain();
        msg.init(MSG_WRITE_DATA, json);
        synchronized (writingQue)
        {
            writingQue.add(0, msg);
        }
    }

    protected synchronized void pumpWritingQueue()
    {
        RequestHandle handle = writingHandle;
        if(handle != null && !handle.isFinished() && !handle.isCancelled())
        {
            Log.i(tag, "WritingQue: sending is busy, waiting for result..." + writingQue.size());
            return;
        }

        Message msg;

        synchronized (writingQue)
        {
            if(writingQue.isEmpty())
            {
                Log.i(tag, "WritingQue: queue is empty, nothing to send");
                return;
            }

            msg = writingQue.remove(0);
        }

        JSONObject json = (JSONObject) msg.obj;
        Log.i("xpenseweb", "WritingQue: do actual sending");
        writingHandle = doPUT(this.client, mainURL, json, asyncHttpResponseHandler);
    }

    protected void pushWritingQue(JSONObject json)
    {
        Message msg = writingThread.obtain();
        msg.init(MSG_WRITE_DATA, json);
        synchronized (writingQue)
        {
            writingQue.add(msg);
        }

        pumpWritingQueue();
    }

    protected class AsyncHandlerWeb extends AsyncHttpResponseHandler
    {
        int retOK, retErr;
        int errorCounter = 0;
        int MAX_ERR_COUNT = 3;

        public AsyncHandlerWeb(int ok, int err)
        {
            super();
            retOK = ok;
            retErr = err;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
        {
            JSONObject decodedJSON = null;
            errorCounter = 0;
            if(responseBody.length > 0)
            {
                try
                {
                    String retCode = byte2string(responseBody);

                    decodedJSON = new JSONObject(retCode);
                }
                catch (Exception jex)
                {
                    onFailure(statusCode, headers, responseBody, jex);
                    return;
                }
            }

            sendSuccess(decodedJSON);
        }

        protected void sendSuccess(JSONObject decodedJSON)
        {
            if(!sendMainMessage(retOK, decodedJSON))
                pumpWritingQueue();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
        {
            errorCounter++;
            Log.e("xpenseweb", "Basic Write failure with " + error);

            if(errorCounter > MAX_ERR_COUNT || !retryOnError(statusCode, error))
            {
                ThreadInfo einfo = new ThreadInfo(new LogicException(
                        "remCouldNotSend"));
                sendFailure(einfo);
                errorCounter = 0;
            }
        }

        private long lastDelay = 0L;
        private void doDelay()
        {
            try
            {
                long time = System.currentTimeMillis();
                if(time - lastDelay < 2000)
                    Thread.sleep(2000);
                lastDelay = System.currentTimeMillis();
            } catch (InterruptedException e1)
            {
            }
        }

        protected void sendFailure(ThreadInfo einfo)
        {
            Log.e("xpenseweb", "Basic Write send failure with " + einfo.ex);
            sendMainMessage(retErr, einfo);
        }

        /**
         * Retry failed request or just return false for passing error to main
         * @param statusCode
         * @param error
         * @return
         */
        protected boolean retryOnError(int statusCode, Throwable error)
        {
            Log.e("xpenseweb", "Basic write retry request " + errorCounter + "/" + MAX_ERR_COUNT);
            if(lastJSON != null)
            {
                doDelay();
                writeBackToWritingQueue(this.lastJSON);
                pumpWritingQueue();
                return true;
            }

            return false;
        }

        public JSONObject lastJSON = null;

        public void setLastJSON(JSONObject json)
        {
            lastJSON = json;
        }

        /**
         * Fired in all cases when the request is finished, after both success and failure, override to
         * handle in your own code
         */
        @Override
        public void onFinish()
        {
            Log.i("xpenseweb", "Basic write request finished");
        }
    };


    protected AsyncHandlerWeb asyncHttpResponseHandler;

    @Override
    public synchronized void startTransfer()
    {
        Log.d(tag, "Starting write thread...");
        if(connectionStatus == TERMINATED)
            setConnectionStatus(STOPPED);

        if(writingThread != null)
        {
            writerInited.open();
        } else
        {
            writingThread = new WritingThread()
            {
                @Override
                protected void onLooperPrepared()
                {
                    super.onLooperPrepared();
                    writerInited.open();
                }

                @Override
                protected boolean processMsg(Message msg)
                {
                    super.processMsg(msg);
                    return true;
                }
            };
        }
    }



	/* (non-Javadoc)
	 * @see northern.captain.vendingman.tools.ExtComProcessor#writeMessageThreaded(android.os.Message)
	 */
	@Override
	protected void writeMessageThreaded(Message msg) throws IOException, LogicException
	{
	}

    protected String byte2string(byte[] data)
    {
        String inbuf = new String(data);

        return Encodeco.decodeS4(inbuf, S4Factory.instance());
    }

    protected String encodeString(String from, String type)
    {
        return Encodeco.encodeS4(from, S4Factory.instance());
    }

	/**
	 * Abort all active http requests
	 */
	protected void abortAllRequests()
	{
		Log.d(tag, "Aborting all requests.");

        if(writingHandle != null)
            writingHandle.cancel(true);
	}
	
	protected void setHttpHeader(AsyncHttpClient client)
	{
		client.addHeader("Accept", "application/json");
		client.setUserAgent("Apache-HttpClient/XPense 1.0");
        client.addHeader("Content-type", "application/json");
        client.removeHeader("Accept-Encoding");
	}

	protected RequestHandle doPUT(HttpClientHolder cln, String url, JSONObject toPut, AsyncHandlerWeb handler)
	{
		String msgtype = null;
        String urlp = null;
		try
		{
			toPut.put("v", AndroidContext.VERSION);
            if(toPut.has("urlp"))
            {
                urlp = toPut.getString("urlp");
            }

            msgtype = toPut.getString("type");
            if(urlp == null)
                urlp = msgtype;
		}
		catch (JSONException ex1)
		{
		}

        url += urlp;

        Log.d(tag, "doPUT: .." + url.substring(url.length() - 15));

        handler.setLastJSON(toPut);

        String toSend = encodeString(toPut.toString(), msgtype);

        try
        {
            HttpEntity entity = new StringEntity(toSend, "UTF-8");
            return cln.client.put(null, url, entity,
                    "application/json", handler);
        } catch(UnsupportedEncodingException ex) {}

        return null;
 	}

	/**
	 * @return boolean return true if the application can access the internet
	 */
	public static synchronized String haveInternet()
	{
		NetworkInfo info=((ConnectivityManager) AndroidContext.mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE)).
				getActiveNetworkInfo();
		if(info == null || !info.isConnected())
		{
			return "remInternetLost";
		}
		return null;
	}
	
	@Override
	public void doStop()
	{
		super.doStop();
		abortAllRequests();
	}
	
	@Override
	public void doResume()
	{
		if(connectionStatus != TERMINATED)
			return;
		super.doResume();
	}
	
    /**
     * Starts the processor, initialize and establish connection through
     * starting connect thread
     */
    @Override
    public void doStart()
    {
        if(connectionStatus == TERMINATED)
            return;

        Log.d(tag, "doStart called, processing");
        if(writingThread == null)
            startTransfer();

        if(connectionStatus == STOPPED)
            setConnectionStatus(NOT_CONNECTED);

        if(connectionStatus != NOT_CONNECTED)
        {
            doStop();
            setConnectionStatus(NOT_CONNECTED);
        }

        connect();
    }

    /**
     * Send BLOCK command to the writer thread. Writer will block itself on receive of this command
     */
    public void doBlock()
    {
        Log.d(tag, "doBlock call, sending block command");
        writerInited.open();
        setConnectionStatus(TERMINATED);
    }

    int sendConfirmId = 0;

    /**
     * Called in the main processing thread with the purpose to send data to the writing thread
     */
    @Override
    public void sendToRemote(JSONObject json)
    {
        sendToRemote(null, json);
    }

    @Override
    public void sendToRemote(String type, JSONObject json)
    {
        try
        {
            String intype = json.getString("type");
            if(type != null && !type.equals(intype))
            {
                json.put("urlp", type);
            }
            json.put("v", AndroidContext.VERSION);
            json.put("wcfm", sendConfirmId++);
        } catch (JSONException e)
        {
            Log.e(tag, "could not find type in JSON: " + json.toString());
            return;
        }

        pushWritingQue(json);
    }

    protected void checkZFile(JSONObject json)
    {

    }

    @Override
    protected void onWriterDeath(ThreadInfo writerTh)
    {
        Log.d(tag, "on writer death event");
        if(isShutdown
                || connectionStatus == TERMINATED
                || connectionStatus == CONNECTING)
            return;

        doStop();
        communicationError(writerTh.ex, "Exchange problem, reconnecting.");
        if(!isShutdown)
            doStart();
    }

}
