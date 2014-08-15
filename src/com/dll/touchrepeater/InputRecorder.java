package com.dll.touchrepeater;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class InputRecorder implements Runnable {

    static {
	System.loadLibrary("inputrecorder");
    }

    private static final String TAG = InputRecorder.class.getName();

    protected boolean isRecording;

    protected String recordFilePath;

    protected Handler handler;

    protected InputRecorderListener listener;

    private static InputRecorder instance;

    protected long recorderPointer;

    private InputRecorder() {
	super();
	isRecording = false;
	handler = new Handler(Looper.getMainLooper(), new RecorderHandle());
    }

    public static InputRecorder getInstance() {
	if (instance == null) {
	    instance = new InputRecorder();
	}
	return instance;
    }

    /**
     * 是否正在录制输入事件
     * 
     * @return
     */
    public boolean isRecording() {
	return isRecording;
    }

    /**
     * 开始录制输入事件。
     * 
     * @param filePath
     *            存储的文件路径
     * @return 是否成功，如果失败可能是未获取root权限。只有当返回true时会调用回调接口，返回false不会回调。
     */
    public synchronized boolean record(String recordFilePath) {
	if (isRecording) {
	    return true;
	}
	long pointer;
	if ((pointer = nativeInit(recordFilePath)) != 0) {
	    isRecording = true;
	    this.recordFilePath = recordFilePath;
	    recorderPointer = pointer;
	    new Thread(this).start();
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * 停止录制输入事件
     * 
     */
    public void stop() {
	if (isRecording) {
	    nativeStop(recorderPointer);
	    isRecording = false;
	    Log.i(TAG, "record stop");
	    handler.obtainMessage(RecorderHandle.WHAT_FINISH).sendToTarget();
	}
    }

    private native void nativeStop(long recorderPointer);

    private native int nativeRecord(long recorderPointer);

    private native long nativeInit(String filePath);


    @Override
    public void run() {
	if (nativeRecord(recorderPointer) == 0) {
	    Log.i(TAG, "record thread end");
	} else {
	    Log.i(TAG, "record thread failed");
	    isRecording = false;
	    handler.obtainMessage(RecorderHandle.WHAT_FAILED).sendToTarget();
	}
    }

    public void setRecorderListener(InputRecorderListener listener) {
	this.listener = listener;
    }

    /**
     * InputRecorder的回调接口。
     * 
     * @author DLL email: xiaobinlzy@163.com
     * 
     */
    public static interface InputRecorderListener {

	/**
	 * 录制完成后调用
	 * 
	 * @param recorder
	 * @param filePath
	 */
	public void onRecorderFinish(InputRecorder recorder, String filePath);

	/**
	 * 录制失败后调用。
	 * 
	 * @param recorder
	 * @param filePath
	 */
	public void onRecorderFailed(InputRecorder recorder, String filePath);
    }

    private class RecorderHandle implements Callback {

	private static final int WHAT_FINISH = 0;
	private static final int WHAT_FAILED = 1;

	@Override
	public boolean handleMessage(Message msg) {
	    if (listener == null) {
		return false;
	    }
	    switch (msg.what) {
	    case WHAT_FINISH:
		listener.onRecorderFinish(InputRecorder.this, recordFilePath);
		break;
	    case WHAT_FAILED:
		listener.onRecorderFailed(InputRecorder.this, recordFilePath);
		break;
	    }
	    return false;
	}

    }

}
