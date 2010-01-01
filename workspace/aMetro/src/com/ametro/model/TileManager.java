package com.ametro.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.util.Log;

import com.ametro.MapSettings;
import com.ametro.MapUri;

public class TileManager {

	private TileManagerDescription mDescription;
	private int mLevel = 0;
	//private Dictionary<String, byte[]> mFileCache;

	private TileManager(Uri uri) throws IOException, ClassNotFoundException {
		String mapName = MapUri.getMapName(uri);
		//mFileCache = new Hashtable<String, byte[]>();
		mDescription = readDescription(mapName);
	}

	public Point getContentSize() {
		return new Point(mDescription.width, mDescription.height);
	}

	public Bitmap getTile(Rect rect) {
		if(rect.width() == MapSettings.TILE_WIDTH 
				&& rect.height() == MapSettings.TILE_HEIGHT
				&& (rect.left % MapSettings.TILE_WIDTH) == 0
				&& (rect.top % MapSettings.TILE_HEIGHT) == 0
		){
			int column = rect.left / MapSettings.TILE_WIDTH;
			int row = rect.top / MapSettings.TILE_HEIGHT;
			return loadTile(mDescription.mMapName, row, column, mLevel);
		}
		return null;
	}

	public String getCityName() {		
		return mDescription.mCityName;
	}

	public String getContryName() {
		return mDescription.countryName;
	}


	public static boolean isExist(String mapName, int level)
	{
		File mapFile = new File(MapSettings.CATALOG_PATH + mapName + ".pmz");
		File tilesDescription = new File( getCacheRootPath(mapName) + ".description" );
		return mapFile.exists() && tilesDescription.exists() && mapFile.lastModified() <= tilesDescription.lastModified();
	}

	public static TileManager load(Uri uri) throws IOException, ClassNotFoundException{
		return new TileManager(uri);
	}

	public static void recreate(Model model, IProgressUpdate progress) throws IOException{
		cleanupTiles(model.getMapName(), 0);
		writeModelTiles(model, progress);
		writeDescription(new TileManagerDescription(model));
	}

	private static void writeDescription(TileManagerDescription description) throws IOException {
		String descriptionFileName = getCacheRootPath(description.mMapName) + ".description";
		ObjectOutputStream strm = null;
		try{
			strm = new ObjectOutputStream(new FileOutputStream(descriptionFileName));
			strm.writeObject(description);
			strm.flush();
		} finally{
			if(strm!=null){
				strm.close();
			}
		}
	}

	private static TileManagerDescription readDescription(String mapName) throws IOException, ClassNotFoundException
	{
		String descriptionFileName = getCacheRootPath(mapName) + ".description";
		ObjectInputStream strm = null;
		try{
			strm = new ObjectInputStream(new FileInputStream(descriptionFileName));
			return (TileManagerDescription)strm.readObject();
		} finally{
			if(strm!=null){
				strm.close();
			}
		}

	}

	private static void writeModelTiles(Model model, IProgressUpdate progress) {

		//writeModelTilesEntire(model);
		writeModelTilesStepByStep(model, progress);
	}

	private static void writeModelTilesStepByStep(Model model, IProgressUpdate progress) {
		int y = 0;
		int height = model.getHeight();
		int width = model.getWidth();
		int columns = width / MapSettings.TILE_WIDTH + ( width % MapSettings.TILE_WIDTH != 0 ? 1 : 0 );
		int heightStepMax = Math.max(MapSettings.TILE_HEIGHT, MapSettings.TILE_HEIGHT * 50 / columns);
		int step = Math.min(MapSettings.TILE_HEIGHT, heightStepMax - (heightStepMax % MapSettings.TILE_HEIGHT));
		int row = 0; 
		if(progress!=null) progress.update(0);
		while(y<height){
			int renderHeight = Math.min(step, height - y);

			Rect renderRect = new Rect(0, y, width, y + renderHeight);
			Bitmap buffer = Bitmap.createBitmap(width, renderHeight, Config.RGB_565 );
			Canvas bufferCanvas = new Canvas(buffer);
			model.render(bufferCanvas,renderRect);

			Rect src = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
			TileManager.createTiles(model.getCityName(), row, 0, buffer, src, 0);
			buffer.recycle();
			buffer = null;

			y += renderHeight;
			row += (renderHeight / MapSettings.TILE_HEIGHT);
			if(progress!=null) progress.update( 100 * y / height );
		}
	}

//	private static void writeModelTilesEntire(Model model) {
//		Rect renderRect = new Rect(0, 0, model.getWidth(), model.getHeight());
//		Bitmap buffer = Bitmap.createBitmap(model.getWidth(), model.getHeight(), Config.RGB_565 );
//		Canvas bufferCanvas = new Canvas(buffer);
//		model.render(bufferCanvas,renderRect);		
//		Rect src = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
//		TileManager.createTiles(model.getCityName(), 0, 0, buffer, src, 0);
//	}

	private static String getTileFileName(int row, int column ){
		return "tile_" + row + "_" + column + ".png";
	}

	public static String getCachePath(String mapName, int level){
		return (MapSettings.CATALOG_PATH + "/cache/" + mapName + "/" + Integer.toString(level)).toLowerCase() + "/";
	}

	public static String getCacheRootPath(String mapName){
		return (MapSettings.CATALOG_PATH + "/cache/" + mapName + "/").toLowerCase();
	}

	private Bitmap loadTile(String mapName, int row, int column, int level){
		String path = getCachePath(mapName, level);
		String fileName = path + getTileFileName(row, column);
		return BitmapFactory.decodeFile(fileName);
	}


	private static void cleanupTiles(String mapName, int level)
	{
		File dir = new File( getCachePath(mapName, level));
		String[] files = dir.list();
		if(files!=null){
			for (int i = 0; i < files.length; i++) {
				File picture = new File(files[i]);
				picture.delete();
			}
		}
	}

	private static void createTiles(String name, int row, int column, Bitmap buffer, Rect bufferRect, int level) {
		String path = getCachePath(name, level);
		File dir = new File(path);
		if(!dir.exists()){
			dir.mkdirs();
		}
		int height = bufferRect.height();
		int width = bufferRect.width(); 
		int maxRow = row + (height / MapSettings.TILE_HEIGHT) + ((height % MapSettings.TILE_HEIGHT)!= 0 ? 1 : 0);
		int maxColumn = column + (width / MapSettings.TILE_WIDTH) + ((width % MapSettings.TILE_WIDTH)!= 0 ? 1 : 0);
		Bitmap bmp = Bitmap.createBitmap(MapSettings.TILE_WIDTH, MapSettings.TILE_HEIGHT, Config.RGB_565);
		Rect dst = new Rect(0,0,MapSettings.TILE_WIDTH,MapSettings.TILE_HEIGHT);
		for(int i = row; i < maxRow; i++ )
		{
			for(int j = column; j < maxColumn; j++){
				String fileName = path + getTileFileName(i, j);
				int left = bufferRect.left + (j-column) * MapSettings.TILE_WIDTH;
				int top = bufferRect.top + (i-row) * MapSettings.TILE_HEIGHT;
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
