package com.ametro;

import com.ametro.model.TileManager;
import com.ametro.widgets.TileImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class BrowseTileMap extends Activity implements TileImageView.IDataProvider{
	
	TileImageView mTileImageView;
	TileManager mTileManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Uri uri = Uri.parse("ametro://moscow");
		mTileManager = new TileManager(this, uri);
		
		mTileImageView = new TileImageView(this);
		mTileImageView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		FrameLayout layout = new FrameLayout(this);
		layout.addView(mTileImageView);
		setContentView(layout);

		mTileImageView.setDataProvider(this);
		
	}
	
	@Override
	public Bitmap getTile(Rect rect) {
		return mTileManager.getTile(rect);
	}

	@Override
	public Bitmap getLoadingTile() {
		return BitmapFactory.decodeResource(getResources(), R.drawable.tile);
	}

	@Override
	public Point getContentSize() {
		return mTileManager.getContentSize();
	}

}
