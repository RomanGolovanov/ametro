package com.ametro.model;

import android.graphics.Bitmap;

public class Tile {

	public static final float SCALE = 1.3f;
	public static final int MIP_MAP_LEVELS = 3;
	
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
	
	public static float getScale(int level){
		if(level == 0) return 1.0f;
		return (float)Math.exp( level * Math.log(Tile.SCALE) ) ;//(level+1) * Tile.SCALE;
	}
	
	public static int getDimension(int base, int level){
		if(level == 0) return base;
		return (int) ((float)base / getScale(level));
	}
	
	public static int getTileCount(int dimensionSize){
		return dimensionSize / WIDTH + ( (dimensionSize % WIDTH)!=0 ? 1 : 0);
	}
		
	public static int getTileCount(int dimensionSize, int level){
		if(level == 0) return getTileCount(dimensionSize);
		int newSize = getDimension(dimensionSize, level);
		return newSize / WIDTH + ( (newSize % WIDTH)!=0 ? 1 : 0);
	}
		
}
