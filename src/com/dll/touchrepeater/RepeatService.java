package com.dll.touchrepeater;

import java.io.File;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.dll.touchrepeater.InputRecorder.InputRecorderListener;
import com.dll.touchrepeater.InputReplayer.InputReplayerListener;
import com.dll.touchrepeater.RepeatLayout.OnEventListener;
import com.dll.util.FilePathUtil;

public class RepeatService extends Service {

    private static final String TAG = RepeatService.class.getName();

    protected InputRecorder recorder;

    protected InputReplayer replayer;

    protected RepeatLayout layout;

    protected LayoutParams layoutParams;

    protected WindowManager manager;

    protected boolean isAddedToWindow;

    protected Button btnRecord, btnReplay;

    protected static final String RECORD_FILE_NAME = "record_events";

    protected static final String RECORD_FOLDER = "record";

    protected String recordFilePath;

    protected float originX, originY, startX, startY;

    protected boolean isInterceptEvent;

    protected ServiceListener listener;

    private static final int SERVICE_ID = 1023;

    protected Notification notification;

    @Override
    public void onCreate() {
	super.onCreate();
	listener = new ServiceListener();
	String[] commands = new String[8];
	for (int i = 0; i < commands.length; i++) {
	    commands[i] = "chmod 777 /dev/input/event" + i + "\n";
	}
	RootPermission.rootPermission(commands);
	recorder = InputRecorder.getInstance();
	replayer = InputReplayer.getInstance();
	recordFilePath = FilePathUtil.makeFilePath(this, RECORD_FOLDER, RECORD_FILE_NAME);
	initLayout();
	recorder.setRecorderListener(listener);
	replayer.setReplayerListener(listener);

	notification = new Notification();
	notification.flags = Notification.FLAG_NO_CLEAR;
	notification.tickerText = "记录操作";
	RemoteViews remoteViews = new RemoteViews(getPackageName(), android.R.layout.simple_gallery_item);
	remoteViews.setTextViewText(android.R.id.text1, "记录操作");
	notification.contentView = remoteViews;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	addToWindow();
	startForeground(SERVICE_ID, notification);
	return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
	Log.i(TAG, "repeat service destroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    protected void onClickRecord() {
	if (!recorder.isRecording()) {
	    if (recorder.record(recordFilePath)) {
		btnRecord.setText(R.string.button_stopRecording);
		btnReplay.setEnabled(false);
	    } else {
		Toast.makeText(this, "录制失败，请检查root权限", Toast.LENGTH_SHORT).show();
	    }
	} else {
	    recorder.stop();
	    btnRecord.setText(R.string.button_startRecording);
	}
    }

    protected void onClickReplay() {
	if (!replayer.isReplaying()) {
	    if (replayer.replay(recordFilePath, 1)) {
		btnReplay.setText(R.string.button_stopReplaying);
		btnRecord.setEnabled(false);
	    } else {
		Toast.makeText(this, "重放失败，请检查文件是否正确", Toast.LENGTH_SHORT).show();
	    }
	} else {
	    replayer.stop();
	    btnReplay.setText(R.string.button_startReplaying);
	}
    }

    protected void initLayout() {
	layoutParams = new WindowManager.LayoutParams();
	layout = (RepeatLayout) LayoutInflater.from(this).inflate(R.layout.repeat_layout, null);
	layout.setEventListener(listener);
	manager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
	layoutParams.type = LayoutParams.TYPE_SYSTEM_ERROR;
	layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
	layoutParams.format = PixelFormat.RGBA_8888;
	layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
	layoutParams.x = 0;
	layoutParams.y = 0;
	layoutParams.width = LayoutParams.WRAP_CONTENT;
	layoutParams.height = LayoutParams.WRAP_CONTENT;
	layout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
		View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
	btnRecord = (Button) layout.findViewById(R.id.button_record);
	btnReplay = (Button) layout.findViewById(R.id.button_replay);
	btnRecord.setOnClickListener(listener);
	btnReplay.setOnClickListener(listener);
	if (!new File(recordFilePath).isFile()) {
	    btnReplay.setEnabled(false);
	}
    }

    protected void addToWindow() {
	if (isAddedToWindow) {
	    return;
	}
	isAddedToWindow = true;
	manager.addView(layout, layoutParams);
    }

    public static void startService(Context context) {
	Intent intent = new Intent(context, RepeatService.class);
	context.startService(intent);
    }

    private class ServiceListener implements OnClickListener, OnEventListener,
	    InputRecorderListener, InputReplayerListener {

	@Override
	public void onClick(View v) {
	    Log.i(TAG, "on click");
	    switch (v.getId()) {
	    case R.id.button_record:
		onClickRecord();
		break;
	    case R.id.button_replay:
		onClickReplay();
		break;
	    }
	}

	@Override
	public void onDispatchTouchEvent(MotionEvent ev) {
	}

	@Override
	public void onReplayerFinish(InputReplayer replayer, String filePath) {
	    btnRecord.setEnabled(true);
	    btnReplay.setText(R.string.button_startReplaying);
	}

	@Override
	public void onReplayerFailed(InputReplayer replayer, String filePath) {
	    btnRecord.setEnabled(true);
	    Toast.makeText(RepeatService.this, "重放失败", Toast.LENGTH_SHORT).show();
	    btnReplay.setText(R.string.button_startReplaying);
	}

	@Override
	public void onRecorderFinish(InputRecorder recorder, String filePath) {
	    btnReplay.setEnabled(true);
	}

	@Override
	public void onRecorderFailed(InputRecorder recorder, String filePath) {
	    btnReplay.setEnabled(true);
	    Toast.makeText(RepeatService.this, "录制失败", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
		startX = event.getRawX();
		startY = event.getRawY();
		originX = layoutParams.x;
		originY = layoutParams.y;
		isInterceptEvent = false;
		break;
	    case MotionEvent.ACTION_UP:
	    default:
		layoutParams.x = Math.round(event.getRawX() - startX + originX);
		layoutParams.y = Math.round(event.getRawY() - startY + originY);
		isInterceptEvent = isInterceptEvent || Math.abs(event.getRawX() - startX) > 2
			|| Math.abs(event.getRawY() - startY) > 2;
		break;
	    }
	    manager.updateViewLayout(layout, layoutParams);
	    return event.getAction() == MotionEvent.ACTION_UP ? isInterceptEvent : false;
	}
    }
}
