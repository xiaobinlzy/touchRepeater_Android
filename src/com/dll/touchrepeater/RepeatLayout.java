package com.dll.touchrepeater;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class RepeatLayout extends LinearLayout {

    protected OnEventListener eventListener;

    public RepeatLayout(Context context) {
	super(context);
    }

    public RepeatLayout(Context context, AttributeSet attrs) {
	super(context, attrs);
    }

    public void setEventListener(OnEventListener listener) {
	this.eventListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
	if (this.eventListener != null) {
	    this.eventListener.onDispatchTouchEvent(ev);
	}
	return super.dispatchTouchEvent(ev);
    }

    public static interface OnEventListener {
	public void onDispatchTouchEvent(MotionEvent ev);

	public boolean onInterceptTouchEvent(MotionEvent ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
	if (this.eventListener == null) {
	    return super.onInterceptTouchEvent(ev);
	} else {
	    return this.eventListener.onInterceptTouchEvent(ev);
	}
    }
}
