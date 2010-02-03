package com.ametro.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.net.Uri;

import com.ametro.MapSettings;
import com.ametro.MapUri;

public class ModelTileContainer {

	public static class ModelTileOutputStream {

		private ZipOutputStream content;

		public ModelTileOutputStream(String fileName) throws FileNotFoundException{
			this(new File(fileName));
		}

		public ModelTileOutputStream(File file) throws FileNotFoundException{
			content = new ZipOutputStream(new FileOutputStream(file) );
		}

		public void write(ModelDescription description) throws IOException{
			ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
			description.setRenderVersion(MapSettings.getRenderVersion());
			content.putNextEntry(entry);
			ObjectOutputStream strm = new ObjectOutputStream(content);
			strm.writeObject(description);
			strm.flush();
			content.closeEntry();		
		}

		public void write(Tile tile) throws IOException{
			Bitmap bmp = tile.getImage();
			String fileName = ModelTileContainer.getTileEntityName(tile.getMapMapLevel(), tile.getRow(), tile.getColumn()); 
			ZipEntry entry = new ZipEntry(fileName);
			content.putNextEntry(entry);
			content.setLevel(-1);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, content);
			content.flush();
			content.closeEntry();
		}

		public void close() throws IOException{
			content.close();
			content = null;
		}

	}

	private int mLevel = 0;
	private ModelDescription mDescription; 
	private ZipFile mContent;

	public boolean zoomIn(){
		if(mLevel>0){
			mLevel--;
			return true;
		}
		return false;
	}

	public boolean zoomOut(){
		if((mLevel+1) < Tile.MIP_MAP_LEVELS){
			mLevel++;
			return true;
		}
		return false;
	}	
	private ModelTileContainer(Uri uri, int level) throws IOException, ClassNotFoundException {
		String mapName = MapUri.getMapName(uri);
		mLevel = level;
		final String fileName = MapSettings.getCacheFileName(mapName);
		mDescription = ModelBuilder.loadModelDescription(fileName);
		mContent = new ZipFile(fileName);
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

	public static ModelTileContainer load(Uri uri, int level) throws IOException, ClassNotFoundException{
		return new ModelTileContainer(uri,level);
	}


	public static String getTileEntityName(int level, int row, int column ){
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
		return new Point( 
				Tile.getDimension(mDescription.getWidth(), mLevel) , 
				Tile.getDimension(mDescription.getHeight(), mLevel)  
				);
	}

	public Object getCityName() {
		return mDescription.getCityName();
	}
}
