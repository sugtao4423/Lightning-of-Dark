package com.tao.lightning_of_dark.swipeImageViewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ImageViewPager extends ViewPager{

	private int pageSize;

	public ImageViewPager(Context context){
		super(context);
		this.pageSize = -1;
	}

	public ImageViewPager(Context context, AttributeSet attrs){
		super(context, attrs);
		this.pageSize = -1;
	}

	public void setPageSize(int pageSize){
		this.pageSize = pageSize;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		if(pageSize == 1)
			return false;
		else
			return super.onInterceptTouchEvent(event);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(pageSize == 1)
			return false;
		else
			return super.onTouchEvent(event);
	}
}
