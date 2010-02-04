package com.ametro.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.ametro.MapSettings;
import com.ametro.MapUri;

public class TileContainer {

	private ZipFile mContent;

	private ModelDescription mDescription; 
	
	public Bitmap getTile(Rect rect, int mipMapLevel) {
		final int column = rect.left / Tile.WIDTH;
		final int row = rect.top / Tile.HEIGHT;
		return loadTile(row, column, mipMapLevel);
	}

	public static boolean isExist(String mapName)
	{
		File mapFile = new File(MapSettings.getMapFileName(mapName));
		File contentFile = new File(MapSettings.getCacheFileName(mapName));
		if( mapFile.exists() && contentFile.exists()){
			try{
				ModelDescription cacheDescription = ModelBuilder.loadModelDescription(contentFile.getAbsolutePath());
				ModelDescription mapDescription = ModelBuilder.loadModelDescription(mapFile.getAbsolutePath());
				return cacheDescription.completeEqual(mapDescription) 
				&& cacheDescription.getRenderVersion() == MapSettings.getRenderVersion()
				&& cacheDescription.getSourceVersion() == MapSettings.getSourceVersion();
			}catch(Exception ex){}

		}
		return false;
	}

	public static String getTileEntityName(int level, int row, int column ){
		return "tile_" + level + "_" + row + "_" + column + ".png";
	}

	public Point getContentSize(int mipMapLevel) {
		return new Point( 
				Tile.getDimension(mDescription.getWidth(), mipMapLevel) , 
				Tile.getDimension(mDescription.getHeight(), mipMapLevel)  
		);
	}

	public Object getCityName() {
		return mDescription.getCityName();
	}
	
	public TileContainer(Uri uri) throws IOException, ClassNotFoundException {
		String mapName = MapUri.getMapName(uri);
		final String fileName = MapSettings.getCacheFileName(mapName);
		mDescription = ModelBuilder.loadModelDescription(fileName);
		mContent = new ZipFile(fileName);
	}	

	private Bitmap loadTile(int row, int column, int level){
		Log.d("aMetro", "Load tile at " + row + "x" + column + " for level " + level );
		String fileName = getTileEntityName(level, row, column);
		ZipEntry f = mContent.getEntry(fileName);
		InputStream fis = null;
		try {
			fis = mContent.getInputStream( f );
			return BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			return null;
		} finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (Exception e) {	}
			}
		}
	}	

	
}
