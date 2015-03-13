package northern.captain.vendingman.tools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

public class ExtComProcessor implements IRemoteDataProc
{
	private final static String tag = "xpenseex";
	
	protected static final int MSG_READER_DIED = 2;
	protected static final int MSG_WRITER_DIED = 3;
	protected static final int MSG_CONNECTED = 4;
	protected static final int MSG_CONNECT_ERR = 5;
	protected static final int MSG_READ_DATA = 6;
	protected static final int MSG_WRITE_DATA = 7;
	protected static final int MSG_STOP =8;
	protected static final int MSG_INFO = 9;
	protected static final int MSG_BLOCK = 10;
	protected static final int MSG_REFUSED = 11;
	protected static final int MSG_BANNED = 12;
    protected static final int MSG_EMPTY = 14;
    protected static final int MSG_CMD_RESPONSE_OK = 101;
    protected static final int MSG_CMD_RESPONSE_ERR = 102;

	public static final int NOT_CONNECTED = 0;
	public static final int CONNECTING = 1;
	public static final int CONNECTED = 2;
	public static final int STOPPED = 3;
	public static final int TERMINATED = 4;


	protected IRemoteDataProc clientCallback;
	
	protected static IRemoteDataProc defaultClientCallback;
	
	protected int connectionStatus;
	protected int connectionCounter = 0;

    public volatile IntHolder readStatus = new IntHolder(0);
    public volatile IntHolder writeStatus = new IntHolder(1);
    public volatile IntHolder rivalStatus = new IntHolder(1);

    private int readCycler = 0;
    private int writeCycler = 1;
    private int rivalCycler = 1;

	public class ThreadInfo
	{
		public Exception ex;
		public Thread thread;
		public int counter = 0;
		
		public ThreadInfo(Exception ex)
		{
			thread = Thread.currentThread();
			this.ex = ex;
		}
		public ThreadInfo(Exception ex, int counter)
		{
			thread = Thread.currentThread();
			this.ex = ex;
			this.counter = counter;
		}
	}
	
	public ExtComProcessor()
	{
	}
	
	
	/**
	 * @return the defaultClientCallback
	 */
	public static IRemoteDataProc getDefaultClientCallback()
	{
		return defaultClientCallback;
	}


	/**
	 * @param indefaultClientCallback the defaultClientCallback to set
	 */
	public static void setDefaultClientCallback(IRemoteDataProc indefaultClientCallback)
	{
		defaultClientCallback = indefaultClientCallback;
	}


	/**
	 * True if processor has already been started
	 * @return
	 */
	public boolean isStarted()
	{
		return writingThread != null;
	}

    protected void setWriteStatus(int value)
    {
        writeCycler = (writeCycler + 1) % 2;
        writeStatus.value = value*2 + writeCycler;
    }

    protected void setReadStatus(int value)
    {
        readCycler = (readCycler + 1) % 2;
        readStatus.value = value*2 + readCycler;
        Log.i("xpenseweb", "Read status display: " + readStatus.value);
    }

    protected void setRivalStatus(int value)
    {
        rivalCycler = (rivalCycler + 1) % 2;
        rivalStatus.value = value*2 + rivalCycler;
    }

    public void setClientCallback(IRemoteDataProc callback)
	{
		clientCallback = callback;
		if(clientCallback != null)
			clientCallback.setSender(this);
	}
	
	protected void processMainMessage(android.os.Message msg)
	{
//		if(this.connectionStatus == STOPPED
//			|| this.connectionStatus == TERMINATED)
//			return;
//
		Log.d(tag, "main received message");
		switch(msg.what)
		{
		case MSG_READ_DATA:
			receivedFromRemote((JSONObject)msg.obj);
			break;
		case MSG_BANNED:
		case MSG_REFUSED:
		case MSG_CONNECT_ERR:
			connectError(msg.obj);
			break;
		case MSG_CONNECTED:
			connected();
			break;
		case MSG_READER_DIED:
			onReaderDeath((ThreadInfo)msg.obj);
			break;
		case MSG_WRITER_DIED:
			onWriterDeath((ThreadInfo)msg.obj);
			break;
		case MSG_INFO:
			onInfo((String)msg.obj);
			break;
		}
	}
	
	public int getConnectionStatus()
	{
		return connectionStatus;
	}
	
	public boolean receivedFromRemote(JSONObject data)
	{
		Log.d(tag, "Received new data, processing");
		boolean retVal = false;
		if(clientCallback != null)
        {
			retVal = clientCallback.receivedFromRemote(data);
        } else
        {
            Log.w(tag, "Received data but no client, trying def! " + data.toString());
        }

        if(!retVal)
        {
            if (defaultClientCallback != null)
            {
                retVal = defaultClientCallback.receivedFromRemote(data);
            } else
            {
                Log.w(tag, "Received data but no client AT ALL, drop packet!!!");
            }
        }
		return retVal;
	}

	public void connectError(Object err)
	{
		Log.w(tag, "Connect error: " + err);
		if(connectionStatus == CONNECTING)
			setConnectionStatus(NOT_CONNECTED);
		
		if(clientCallback != null)
		{
			clientCallback.connectError(err);
		} else
        {
            Log.w(tag, "Received error but no client, drop! " + err);
        }
	}
	
	public void onInfo(String infoMessage)
	{
		if(clientCallback != null)
		{
			clientCallback.information(infoMessage);
		}
	}
	
	public void communicationError(Exception ex, String err)
	{
		Log.w(tag, "Communication err: " + ex.getMessage());
		if(clientCallback != null)
		{
			String errDesc = ex instanceof LogicException ? ex.getMessage() : err;
			clientCallback.communicationError(ex, errDesc);
		}
	}
	
	public void connected()
	{
		Log.d(tag, "Connected to remote OK");
    	setConnectionStatus(CONNECTED);

        writerInited.open();

		if(clientCallback != null)
		{
			clientCallback.connected();
		}
	}
		
	protected void onWriterDeath(ThreadInfo writerTh)
	{
		Log.d(tag, "on writer death event");
		if(isShutdown 
			|| connectionStatus == TERMINATED
			|| connectionStatus == CONNECTING)
			return;
		
		if(writingThread != writerTh.thread)
		{
			writerInited.open();
			return;
		}
		
		if(writerTh.counter > 0 && writerTh.counter != connectionCounter)
		{
			Log.w(tag, "Ignoring err writer: " + writerTh.ex.getMessage());
			writerInited.open();
			return;
		}
		
		doStop();
		communicationError(writerTh.ex, "Exchange problem, reconnecting.");
		if(!isShutdown)
			doStart();
	}

	protected void onReaderDeath(ThreadInfo readerTh)
	{
		Log.d(tag, "on reader death event");
		if(isShutdown 
			|| connectionStatus == TERMINATED
			|| connectionStatus == CONNECTING)
			return;
		
		if(readerTh.counter > 0 && readerTh.counter != connectionCounter)
		{
			Log.w(tag, "Ignoring err reader: " + readerTh.ex.getMessage());
			return;
		}
		
		doStop();
		communicationError(readerTh.ex, "Exchange problem, reconnecting.");
		if(!isShutdown)
			doStart();
	}
	

	public void doStartAnyway()
	{
		if(connectionStatus == TERMINATED)
			setConnectionStatus(STOPPED);
		isShutdown = false;
		doStart();
	}
	
	/**
	 * Starts the processor, initialize and establish connection through
	 * starting connect thread
	 */
	public void doStart()
	{
		if(connectionStatus == TERMINATED)
			return;
		
		Log.d(tag, "doStart called, processing");
		if(writingThread != null && !writingThread.isAlive())
			writingThread = null;
		
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
	 * Stops all activity and non-start it again
	 */
    public void doStop()
	{
    	Log.d(tag, "Got stop command, processing StopAll");
		writerInited.close();
		
		connectionCounter++;
		setConnectionStatus(STOPPED);		
	}
	
    /**
     * Terminates current communication process
     */
    public void doTerminate()
    {
    	if(connectionStatus != TERMINATED)
    	{
    		Log.d(tag, "doTerminate called, stopping remote");
    		doBlock();
    		setConnectionStatus(TERMINATED);
    	}
    }
    
    /**
     * Resumes communication after termination
     */
    public void doResume()
    {
    	if(connectionStatus != TERMINATED)
    		return;
    	
  		setConnectionStatus(CONNECTED);
   		writerInited.open();
    }
    /**
     * Send BLOCK command to the writer thread. Writer will block itself on receive of this command
     */
    public void doBlock()
    {
    	Log.d(tag, "doBlock call, sending block command");
    	if(writingThread == null)
    	{
    		writerInited.close();
    		setConnectionStatus(TERMINATED);
    	}
    	else
    	{
    		writerInited.open();
    		setConnectionStatus(TERMINATED);
    		writingThread.send(MSG_BLOCK);
    	}
    }
    
	/**
	 * Called in the main processing thread with the purpose to send data to the writing thread
	 */
	public void sendToRemote(JSONObject data)
	{
        writerInited.block();
		Log.d(tag, "Sending data to remote");
		Message msg = writingThread.obtain();
        msg.init(MSG_WRITE_DATA, data);
		writingThread.send(msg);
	}

    public void sendToRemote(String type, JSONObject data)
    {
        writerInited.block();
        Log.d(tag, "Sending data to remote");
        Message msg = writingThread.obtain();
        msg.init(MSG_WRITE_DATA, data);
        writingThread.send(msg);
    }

    public void setSender(IRemoteDataProc sender)
	{
		// Do not need this here
	}

	/**
	 * Sets the status of the current connection
	 * @param newStatus
	 */
	public synchronized void setConnectionStatus(int newStatus)
	{
		connectionStatus = newStatus;
	}
	/**
	 * Starts separate connection thread that tries to connect to the device
	 */
	protected synchronized void connect()
	{
		if(connectionStatus != NOT_CONNECTED)
			return;
		
		setConnectionStatus(CONNECTING);
		
		connected();		
	}

    public class WritingThread extends HandlerThread
    {
        private volatile Runnable runInThread;

        public void setRunInThread(Runnable runInThread)
        {
            Log.d(tag, "runInThread set to " + runInThread);
            this.runInThread = runInThread;
        }

        public Runnable getRunInThread()
        {
            return runInThread;
        }
        @Override
        public void run()
        {
            if(runInThread == null)
            {
                super.run();
                return;
            }

            onLooperPrepared();
            Runnable localVar = getRunInThread();
            while(localVar != null)
            {
                localVar.run();
                localVar = getRunInThread();
            }
        }
    }

    protected WritingThread writingThread = null;

	/**
	 * Here we start our two transfer threads, one for sending data, another for receiving
	 */
	public synchronized void startTransfer()
	{
		Log.d(tag, "Starting write thread...");
		if(connectionStatus == TERMINATED)
			setConnectionStatus(STOPPED);

        if(writingThread != null && writingThread.isAlive())
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
                    Log.d(tag, "Writing thread ready for commands");
                    try
                    {
                        Thread.sleep(2400);
                    }
                    catch(Exception ex) {}
//TODO                    SourceVerifier vrf = new SourceVerifier();
//                    vrf.doVerification();
                    writerInited.open();
                }

                @Override
                protected boolean processMsg(Message msg)
                {
                    super.processMsg(msg);
                    writeDataThreaded(msg);
                    return true;
                }
            };

            writingThread.start();
        }
	}
	
	public long getSessionId()
	{
		return 0L;
	}
	
	public void setSessionId(long sid)
	{
		
	}

    /**
     * Do preprocessing on the received JSON message before we send it to the main thread
     * We preprocess this message in the receiving thread, not in the main one.
     * @param json
     * @return false meaning that the message was ignored and should be send to the main thread
     */
    protected boolean preprocessBeforeSendingToMain(JSONObject json)
    {
        if(clientCallback != null)
        {
            return clientCallback.processBeforeSendingToMain(json);
        }

        return false;
    }

	protected boolean isShutdown = false;

    public Handler mainHandler;

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
                            return true;
                        }
                    });
        }

        return mainHandler;
    }

    /**
     * Construct and send message back to the main processing thread
     * @param code
     * @param obj
     */
    protected boolean sendMainMessage(int code, Object obj)
    {
        if(obj instanceof JSONObject)
        {
            if(preprocessBeforeSendingToMain((JSONObject) obj))
            {
                return false;
            }
        }

        Handler handler = getMainHandler();
        android.os.Message msg = handler.obtainMessage();

        if(obj == null)
        {
            msg.what = code;
        }
        else
        {
            msg.what = code;
            msg.obj = obj;
        }

        handler.sendMessage(msg);
        return true;
    }

	protected int maxSendTries = 50;
	/**
	 * Writing loop in a separate thread, waits for the message, then writes data to the server
	 */
	protected void writeDataThreaded(Message msg)
	{
        int count = 0;
        int localCounter = 0;

        while(true)
        {
            try
            {
                localCounter = connectionCounter;
                writeMessageThreaded(msg);
                break;
            } catch (IOException e)
            {
                count++;
                Log.e(tag, "wrTh: Error sending message: " + e.getMessage());
                e.printStackTrace();
				writerInited.close();
                if(count >= maxSendTries)
                {
                    sendMainMessage(MSG_WRITER_DIED,
                            new ThreadInfo(new LogicException(
                                    "remCouldNotSend")));
                    break;
                } else
                {
                    sendMainMessage(MSG_WRITER_DIED, new ThreadInfo(e, localCounter));
                }
                try
                {
                    Thread.sleep(5000);
                } catch (InterruptedException e1)
                {
                }
            }
            catch(LogicException lex)
            {
                Log.e(tag, "wrTh: LError sending message: " + lex.getMessage());
				writerInited.close();
                sendMainMessage(MSG_WRITER_DIED, new ThreadInfo(lex));
                break;
            }
        }
	}
		
	/**
	 * Writes one message arrived to the writing loop to the remove server 
	 * @param msg
	 * @throws java.io.IOException
	 */
	protected void writeMessageThreaded(Message msg) throws IOException, LogicException
	{
	}	
	/**
	 * Shuts down the processor and all its threads
	 */
	public void shutdown()
	{
//		writingHandler.sendEmptyMessage(MSG_STOP);
		doTerminate();
		isShutdown = true;
		writerInited.open();
	}

	@Override
	public void information(String infoMessage)
	{
		
	}

    /**
     * Do preprocessing on the received JSON message before we send it to the main thread
     * We preprocess this message in the receiving thread, not in the main one.
     *
     * @param data
     * @return false meaning that the message was ignored and should be send to the main thread
     */
    @Override
    public boolean processBeforeSendingToMain(JSONObject data)
    {
        return false;
    }

    /**
     * Clears instance on the concrete processor
     */
    public void clearInstance()
    {

    }

    protected ConditionVariable writerInited = new ConditionVariable();

    public WritingThread getWritingThread()
    {
        return writingThread;
    }

    public ConditionVariable getWriterInited()
    {
        return writerInited;
    }

    public void setWritingHandler()
    {

    }
}
