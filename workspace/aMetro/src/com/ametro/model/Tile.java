package com.ametro.model;

import android.graphics.Bitmap;

public class Tile {

	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;
	
	private int mMipMapLevel;
	private int mRow;
	private int mColumn;
	private Bitmap mImage;
	
	public int getRow() {
		return mRow;
	}

	public int getColumn() {
		return mColumn;
	}

	public int getMapMapLevel(){
		return mMipMapLevel;
	}
	
	public Bitmap getImage() {
		return mImage;
	}

	public Tile(int row, int column, int mipMapLevel, Bitmap image) {
		super();
		this.mRow = row;
		this.mColumn = column;
		this.mMipMapLevel = mipMapLevel;
		this.mImage = image;
	}

	public void recycle(){
		mImage.recycle();
		mImage = null;
	}	
	
}
