package com.ametro.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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
import com.ametro.libs.IProgressUpdate;

public class ModelTileManager {

	public static class Description  implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2583296356317936507L;

		public String mMapName;
		public String mCityName;
		public String countryName;
		public int width;
		public int height;
		public int minimumLevel;
		public int maximumLevel;

		public Description(Model map){
			mMapName = map.getMapName();
			mCityName = map.getCityName();
			countryName = map.getCountryName();
			width = map.getWidth();
			height = map.getHeight();
			minimumLevel = 0;
			maximumLevel = 0;
		}

	}	

	private int mLevel = 0;
//	private Hashtable<String, byte[]> mFileCache;
	private Description mDescription; 
	private ZipFile mContent;

	private ModelTileManager(Uri uri) throws IOException, ClassNotFoundException {
		String mapName = MapUri.getMapName(uri);
//		mFileCache = new Hashtable<String, byte[]>();
		mContent = new ZipFile(MapSettings.getCacheFileName(mapName));
		mDescription = loadDescription(mContent);
	}

	public Bitmap getTile(Rect rect) {
		if(rect.width() == MapSettings.TILE_WIDTH 
				&& rect.height() == MapSettings.TILE_HEIGHT
				&& (rect.left % MapSettings.TILE_WIDTH) == 0
				&& (rect.top % MapSettings.TILE_HEIGHT) == 0
		){
			int column = rect.left / MapSettings.TILE_WIDTH;
			int row = rect.top / MapSettings.TILE_HEIGHT;
			return loadTile(row, column, mLevel);
		}
		return null;
	}

	public static boolean isExist(String mapName, int level)
	{
		File mapFile = new File(MapSettings.getMapFileName(mapName));
		File contentFile = new File(MapSettings.getCacheFileName(mapName));
		return mapFile.exists() && mapFile.lastModified() <= contentFile.lastModified();
	}

	public static ModelTileManager load(Uri uri) throws IOException, ClassNotFoundException{
		return new ModelTileManager(uri);
	}

	public static void recreate(Model model, IProgressUpdate progress) throws IOException{
		File file = new File(MapSettings.getCacheFileName(model.getMapName()));
		if(file.exists()){
			file.delete();
		}
		ZipOutputStream content = null;
		try{
			content = new ZipOutputStream(new FileOutputStream(file));
			renderDescription(content, model);
			renderTiles(content, model, progress);
		}finally{
			if(content!=null){
				try{ content.close(); }catch(Exception ex){}
			}
		}
	}

	private static void renderDescription(ZipOutputStream content, Model model) throws IOException {
		ZipEntry entry = new ZipEntry(MapSettings.CACHE_DESCRIPTION);
		Description description = new Description(model);

		content.putNextEntry(entry);
		ObjectOutputStream strm = new ObjectOutputStream(content);
		strm.writeObject(description);
		strm.flush();
		content.closeEntry();

	}

	private Description loadDescription(ZipFile content) throws IOException, ClassNotFoundException {
		ObjectInputStream strm = null;
		try{
			strm = new ObjectInputStream( content.getInputStream(content.getEntry(MapSettings.CACHE_DESCRIPTION)) );
			return (Description)strm.readObject();
		} finally{
			if(strm!=null){
				strm.close();
			}
		}

	}

	private static void renderTiles(ZipOutputStream content, Model map, IProgressUpdate progress) {
		int y = 0;
		int height = map.getHeight();
		int width = map.getWidth();
		int columns = width / MapSettings.TILE_WIDTH + (width % MapSettings.TILE_WIDTH!=0?1:0);
		int rows = height / MapSettings.TILE_HEIGHT + (height%MapSettings.TILE_HEIGHT!=0?1:0);
		int heightStepMax = Math.max(MapSettings.TILE_HEIGHT, MapSettings.TILE_HEIGHT * 150 / columns);
		int step = Math.max(MapSettings.TILE_HEIGHT, heightStepMax - (heightStepMax % MapSettings.TILE_HEIGHT));
		int row = 0; 
		if(progress!=null) progress.update(0);

		Log.i("aMetro",String.format("Model %s render size %s x %s, cols: %s, rows: %s, step: %s",
				map.getMapName(),
				Integer.toString(width),
				Integer.toString(height),
				Integer.toString(columns),
				Integer.toString(rows),
				Integer.toString(step/MapSettings.TILE_HEIGHT)
				));
		Date startTimestamp = new Date();
		
		MapRenderer renderer = new MapRenderer(map);
		while(y<height){
			int renderHeight = Math.min(step, height - y);

			Rect renderRect = new Rect(0, y, width, y + renderHeight);
			Bitmap buffer = Bitmap.createBitmap(width, renderHeight, Config.RGB_565 );
			Canvas bufferCanvas = new Canvas(buffer);
			renderer.render(bufferCanvas,renderRect);

			Rect src = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
			ModelTileManager.createTiles(content, row, 0, buffer, src, 0, rows, columns, progress);
			buffer.recycle();
			buffer = null; 

			y += renderHeight;
			row += (renderHeight / MapSettings.TILE_HEIGHT);
			if(progress!=null) progress.update( 100 * y / height );
		}
		Log.i("aMetro", String.format("Model '%s' render time is %sms", map.getMapName(), Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		
	}

	private static void createTiles(ZipOutputStream content, int row, int column, Bitmap buffer, Rect bufferRect, int level, int rowTotal, int columnTotal, IProgressUpdate progress) {

		int height = bufferRect.height();
		int width = bufferRect.width(); 
		int maxRow = row + (height / MapSettings.TILE_HEIGHT) + ((height % MapSettings.TILE_HEIGHT)!= 0 ? 1 : 0);
		int maxColumn = column + (width / MapSettings.TILE_WIDTH) + ((width % MapSettings.TILE_WIDTH)!= 0 ? 1 : 0);
		Bitmap bmp = Bitmap.createBitmap(MapSettings.TILE_WIDTH, MapSettings.TILE_HEIGHT, Config.RGB_565);
		Rect dst = new Rect(0,0,MapSettings.TILE_WIDTH,MapSettings.TILE_HEIGHT);
		for(int i = row; i < maxRow; i++ )
		{
			for(int j = column; j < maxColumn; j++){
				String fileName = getTileEntityName(level, i, j);
				int left = bufferRect.left + (j-column) * MapSettings.TILE_WIDTH;
				int top = bufferRect.top + (i-row) * MapSettings.TILE_HEIGHT;
				int right = Math.min( left + MapSettings.TILE_WIDTH, bufferRect.right );
				int bottom = Math.min( top + MapSettings.TILE_HEIGHT, bufferRect.bottom );
				Rect src = new Rect(left,top,right,bottom);
				dst.right = right - left;
				dst.bottom = bottom - top;

				if(progress!=null) 
					progress.update( (int)( ( i * columnTotal + j ) * 100.0f / (rowTotal * columnTotal)  ) );				
				
				Canvas c = new Canvas(bmp);
				c.drawColor(Color.MAGENTA);
				c.drawBitmap(buffer, src, dst, null);
				c.save();
				try { 
					ZipEntry entry = new ZipEntry(fileName);
					content.putNextEntry(entry);
					content.setLevel(-1);
					bmp.compress(Bitmap.CompressFormat.PNG, 90, content);
					content.flush();
					content.closeEntry();
				} catch (Exception e) {
					Log.e("aMetro", "Cannot write a map tile to " + fileName);
				}	
			}
		}
		bmp.recycle();
	}

	private static String getTileEntityName(int level, int row, int column ){
		return "tile_" + level + "_" + row + "_" + column + ".png";
	}

	private Bitmap loadTile(int row, int column, int level){
		String fileName = getTileEntityName(level, row, column);

		ZipEntry f = mContent.getEntry(fileName);
		InputStream fis = null;
		Bitmap bmp = null;
		try {
			fis = mContent.getInputStream( f );
			bmp = BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			bmp = Bitmap.createBitmap(MapSettings.TILE_WIDTH, MapSettings.TILE_HEIGHT, Config.RGB_565);
		} finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (Exception e) {	}
			}
		}
		return bmp;
	}	

	public Point getContentSize() {
		return new Point(mDescription.width, mDescription.height);
	}

	public Object getCityName() {
		return mDescription.mCityName;
	}
}
