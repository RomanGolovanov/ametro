package org.ametro.utils;

import android.graphics.PointF;

public class AnimationInterpolator {

	public final static int SCALE = 1;
	public final static int SCROLL = 2;
	public final static int SCALE_AND_SCROLL = 3;
	
	private PointF mStartPoint = new PointF();
	private PointF mEndPoint = new PointF();
	private PointF mNowPoint = new PointF();
	
	private float mStartScale;
	private float mEndScale;
	
	private long mPeriod;
	private long mEndTime;
	private long mNowTime;
	
	private int mMode;
	
	public void begin(PointF startPoint, PointF endPoint, Float startScale, Float endScale, long time){
		this.mNowTime = System.currentTimeMillis();
		this.mEndTime = mNowTime + time;
		this.mPeriod = time;
		this.mMode = 0;
		if(startScale!=null && endScale!=null){
			this.mStartScale = startScale;
			this.mEndScale = endScale;
			this.mMode |= SCALE;
		}
		if(startPoint!=null && endPoint!=null){
			this.mStartPoint.set(startPoint);
			this.mEndPoint.set(endPoint);
			this.mNowPoint.set(startPoint);
			this.mMode |= SCROLL;
		}
	}

    public boolean more(){
		return mNowTime < mEndTime; 
	}
	
	public void next(){
		mNowTime = System.currentTimeMillis();
	}
	
	public PointF getPoint(){
		if(mNowTime < mEndTime){
			float k = getProgress();
			float x = mEndPoint.x - k * (mEndPoint.x - mStartPoint.x);
			float y = mEndPoint.y - k * (mEndPoint.y - mStartPoint.y);
			mNowPoint.set(x, y);
			return mNowPoint;
		}else{
			return mEndPoint;
		}
	}
	
	public float getScale(){
		if(mNowTime < mEndTime){
			float k = getProgress();
			return (mEndScale - k * (mEndScale - mStartScale));
		}else{
			return mEndScale;
		}
	}
	
	private float getProgress() {
		return (float)(mEndTime - mNowTime) / mPeriod;
	}

	public boolean hasScale() {
		return (mMode & SCALE)!=0;
	}
	
	public boolean hasScroll() {
		return (mMode & SCROLL)!=0;
	}
	
}
