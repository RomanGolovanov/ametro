package com.ametro.model;

import java.io.Serializable;

public class Rect implements Serializable {

	public int top;
	public int left;
	public int right;
	public int bottom;

	public Rect(int top, int left, int right, int bottom) {
		super();
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}
	
	public Rect(android.graphics.Rect rect) {
		this.top = rect.top;
		this.left = rect.left;
		this.right = rect.right;
		this.bottom = rect.bottom;
	}

	public int width(){
		return right - left;
	}
	
	public int height(){
		return bottom - top;
	}

	private static final long serialVersionUID = 1L;

	public int centerX() {
		return (left+right)/2;
	}

	
	
}
