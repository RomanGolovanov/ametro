package com.ametro.model;

import android.graphics.Bitmap;

public class Tile {

	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;
	
	private int mRow;
	private int mColumn;
	private Bitmap mImage;
	
	public int getRow() {
		return mRow;
	}

	public void setRow(int row) {
		this.mRow = row;
	}

	public int getColumn() {
		return mColumn;
	}

	public void setColumn(int column) {
		this.mColumn = column;
	}

	public Bitmap getImage() {
		return mImage;
	}

	public void setImage(Bitmap image) {
		this.mImage = image;
	}

	public Tile(int row, int column, Bitmap image) {
		super();
		this.mRow = row;
		this.mColumn = column;
		this.mImage = image;
	}

	public void recycle(){
		mImage.recycle();
		mImage = null;
	}	
	
}
