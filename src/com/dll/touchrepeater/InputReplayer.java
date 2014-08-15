package com.dll.touchrepeater;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class InputReplayer implements Runnable {

    static {
	System.loadLibrary("inputreplayer");
    }

    private static final String TAG = InputReplayer.class.getName();

    protected boolean isReplaying;

    protected String replayFilePath;

    protected long replayerPointer;

    private static InputReplayer instance;

    protected InputReplayerListener listener;

    protected Handler handler;

    protected int repeatTimes;

    private InputReplayer() {
	super();
	isReplaying = false;
	handler = new Handler(Looper.getMainLooper(), new ReplayerHandler());
    }

    public static InputReplayer getInstance() {
	if (instance == null) {
	    instance = new InputReplayer();
	}
	return instance;
    }

    public boolean isReplaying() {
	return isReplaying;
    }

    public synchronized boolean replay(String replayFilePath, int repeatTimes) {
	if (isReplaying) {
	    return true;
	}
	long pointer;
	if ((pointer = nativeInit(replayFilePath)) != 0) {
	    isReplaying = true;
	    replayerPointer = pointer;
	    this.replayFilePath = replayFilePath;
	    this.repeatTimes = repeatTimes;
	    new Thread(this).start();
	    return true;
	} else {
	    Log.i(TAG, "replay init failed");
	    return false;
	}
    }

    public synchronized void stop() {
	if (isReplaying) {
	    nativeStop(replayerPointer);
	    isReplaying = false;
	    Log.i(TAG, "replay stop");
	    handler.obtainMessage(ReplayerHandler.WHAT_FINISH).sendToTarget();
	}
    }

    public void setReplayerListener(InputReplayerListener listener) {
	this.listener = listener;
    }

    private native long nativeInit(String replayFilePath);

    private native int nativeReplay(long replayerPointer, int repeatTimes);

    private native void nativeStop(long replayerPointer);

    @Override
    public void run() {
	if (nativeReplay(replayerPointer, repeatTimes) == 0) {
	    Log.i(TAG, "replay thread end");
	    handler.obtainMessage(ReplayerHandler.WHAT_FINISH).sendToTarget();
	} else {
	    Log.i(TAG, "replay thread intercept");
	    handler.obtainMessage(ReplayerHandler.WHAT_FAILED).sendToTarget();
	}
	isReplaying = false;
    }

    public static interface InputReplayerListener {

	/**
	 * 当回放完成时调用
	 * 
	 * @param replayer
	 * @param filePath
	 */
	public void onReplayerFinish(InputReplayer replayer, String filePath);

	/**
	 * 当回放失败时调用
	 * 
	 * @param replayer
	 * @param filePath
	 */
	public void onReplayerFailed(InputReplayer replayer, String filePath);
    }

    private class ReplayerHandler implements Callback {
	private static final int WHAT_FINISH = 0;
	private static final int WHAT_FAILED = 1;

	@Override
	public boolean handleMessage(Message msg) {
	    if (listener == null) {
		return false;
	    }
	    switch (msg.what) {
	    case WHAT_FINISH:
		listener.onReplayerFinish(InputReplayer.this, replayFilePath);
		break;
	    case WHAT_FAILED:
		listener.onReplayerFailed(InputReplayer.this, replayFilePath);
		break;
	    }
	    return false;
	}
    }
}
