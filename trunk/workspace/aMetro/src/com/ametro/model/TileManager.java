package com.ametro.model;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.util.Log;

import com.ametro.BrowseTileMap;
import com.ametro.MapSettings;
import com.ametro.MapUri;

public class TileManager {

	private String mMapName;
	private Model mModel;
	
	private int mWidth = 1050;
	private int mHeight = 1220;
	
	private int mLevel = 0;
	
	public TileManager(BrowseTileMap browseTileMap, Uri uri) {
		mMapName = MapUri.getMapName(uri);
	}

	public Point getContentSize() {
		return new Point(mWidth, mHeight);
	}

	public Bitmap getTile(Rect rect) {
		if(rect.width() == MapSettings.TILE_WIDTH 
				&& rect.height() == MapSettings.TILE_HEIGHT
				&& (rect.left % MapSettings.TILE_WIDTH) == 0
				&& (rect.top % MapSettings.TILE_HEIGHT) == 0
		){
			int column = rect.left / MapSettings.TILE_WIDTH;
			int row = rect.top / MapSettings.TILE_HEIGHT;
			return loadTile(mMapName, row, column, mLevel);
		}
		return null;
	}

	public static String getCachePath(String mapName, int level){
		return (MapSettings.CATALOG_PATH + "/cache/" + mapName + "/" + Integer.toString(level)).toLowerCase() + "/";
	}

	public static String getTileFileName( int row, int column ){
		return "tile_" + row + "_" + column + ".png";
	}
	
	public static Bitmap loadTile(String mapName, int row, int column, int level){
		String path = getCachePath(mapName, level);
		String fileName = path + getTileFileName(row, column);
		return BitmapFactory.decodeFile(fileName);
	}
	
	public static void createTiles(String name, int row, int column, Bitmap buffer, Rect bufferRect, int level) {
		String path = getCachePath(name, level);
		File dir = new File(path);
		if(!dir.exists()){
			dir.mkdirs();
		}
		int maxRow = (bufferRect.height() / MapSettings.TILE_HEIGHT) + ((bufferRect.height() % MapSettings.TILE_HEIGHT)!= 0 ? 1 : 0);
		int maxColumn = (bufferRect.width() / MapSettings.TILE_WIDTH) + ((bufferRect.width() % MapSettings.TILE_WIDTH)!= 0 ? 1 : 0);
		Bitmap bmp = Bitmap.createBitmap(MapSettings.TILE_WIDTH, MapSettings.TILE_HEIGHT, Config.RGB_565);
		Rect dst = new Rect(0,0,MapSettings.TILE_WIDTH,MapSettings.TILE_HEIGHT);
		for(int i = row; i < maxRow; i++ )
		{
			for(int j = column; j < maxColumn; j++){
				String fileName = path + getTileFileName(i, j);
				int left = bufferRect.left + j * MapSettings.TILE_WIDTH;
				int top = bufferRect.top + i * MapSettings.TILE_HEIGHT;
				int right = Math.min( left + MapSettings.TILE_WIDTH, bufferRect.right );
				int bottom = Math.min( top + MapSettings.TILE_HEIGHT, bufferRect.bottom );
				Rect src = new Rect(left,top,right,bottom);
				dst.right = right - left;
				dst.bottom = bottom - top;
				
				Canvas c = new Canvas(bmp);
				c.drawColor(Color.MAGENTA);
				c.drawBitmap(buffer, src, dst, null);
				c.save();
				try {
					FileOutputStream out = new FileOutputStream(fileName);
					bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
					out.flush();
					out.close();
					out = null;
				} catch (Exception e) {
					Log.e("aMetro", "Cannot write a map tile to " + fileName);
				}				
			}
		}
		bmp.recycle();
	}
	
}
