package com.ametro.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ametro.MapSettings;
import com.ametro.MapUri;
import com.ametro.R;
import com.ametro.libs.ProgressInfo;
import com.ametro.model.MapBuilder;
import com.ametro.model.MapRenderer;
import com.ametro.model.Model;
import com.ametro.model.ModelDescription;
import com.ametro.model.ModelTileManager;

public class RenderMap extends Activity {
	
	private class RenderTask extends AsyncTask<Void, ProgressInfo, Void>{

		private boolean mIsCanceled = false;
		private Throwable mFailReason = null;
		
		private ProgressBar mProgressBar;
		private TextView mProgressTitle;
		private TextView mProgressText;
		private TextView mProgressCounter;
		
		public void recreate(Model model){
			final String mapName = model.getMapName();
			final File file = new File(MapSettings.getTemporaryCacheFile(mapName));
			if(file.exists()){
				file.delete();
			}
			ZipOutputStream content = null;
			try{
				content = new ZipOutputStream(new FileOutputStream(file));

				ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
				ModelDescription description = new ModelDescription(model);
				description.setRenderVersion(MapSettings.getRenderVersion());
				content.putNextEntry(entry);
				ObjectOutputStream strm = new ObjectOutputStream(content);
				strm.writeObject(description);
				strm.flush();
				content.closeEntry();

				renderTiles(content, model);
				content.close();
				content = null;
				final File destFile = new File(MapSettings.getCacheFileName(mapName));
				if(!mIsCanceled){
					Log.i("aMetro","Commit model cache");
					file.renameTo(destFile);
				}else{
					Log.i("aMetro","Rollback model cache due cancelation");
					file.delete();
				}
			}catch(Exception ex){
				Log.i("aMetro","Rollback model cache due exception");
				file.delete();
			}finally{
				if(content!=null){
					try{ content.close(); }catch(Exception ex){}
				}
			}
			
		}

		private void renderTiles(ZipOutputStream content, Model map) {
			int y = 0;
			int height = map.getHeight();
			int width = map.getWidth();
			int columns = width / MapSettings.TILE_WIDTH + (width % MapSettings.TILE_WIDTH!=0?1:0);
			int rows = height / MapSettings.TILE_HEIGHT + (height%MapSettings.TILE_HEIGHT!=0?1:0);
			int heightStepMax = Math.max(MapSettings.TILE_HEIGHT, MapSettings.TILE_HEIGHT * 150 / columns);
			int step = Math.max(MapSettings.TILE_HEIGHT, heightStepMax - (heightStepMax % MapSettings.TILE_HEIGHT));
			int row = 0; 

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
				if(mIsCanceled) return;
				int renderHeight = Math.min(step, height - y);

				Rect renderRect = new Rect(0, y, width, y + renderHeight);
				Bitmap buffer = Bitmap.createBitmap(width, renderHeight, Config.RGB_565 );
				Canvas bufferCanvas = new Canvas(buffer);
				
				publishProgress(new ProgressInfo(100 * y / height,100,"Rendering...","Create map image"));
				
				renderer.render(bufferCanvas,renderRect);

				Rect src = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
				createTiles(content, row, 0, buffer, src, 0, rows, columns);
				buffer.recycle();
				buffer = null; 

				y += renderHeight;
				row += (renderHeight / MapSettings.TILE_HEIGHT);
			}
			Log.i("aMetro", String.format("Model '%s' render time is %sms", map.getMapName(), Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
		}

		private void createTiles(ZipOutputStream content, int row, int column, Bitmap buffer, Rect bufferRect, int level, int rowTotal, int columnTotal) {
			int height = bufferRect.height();
			int width = bufferRect.width(); 
			int maxRow = row + (height / MapSettings.TILE_HEIGHT) + ((height % MapSettings.TILE_HEIGHT)!= 0 ? 1 : 0);
			int maxColumn = column + (width / MapSettings.TILE_WIDTH) + ((width % MapSettings.TILE_WIDTH)!= 0 ? 1 : 0);
			Bitmap bmp = Bitmap.createBitmap(MapSettings.TILE_WIDTH, MapSettings.TILE_HEIGHT, Config.RGB_565);
			Rect dst = new Rect(0,0,MapSettings.TILE_WIDTH,MapSettings.TILE_HEIGHT);
			for(int i = row; i < maxRow; i++ )
			{
				for(int j = column; j < maxColumn; j++){
					if(mIsCanceled) return;
					
					String fileName = ModelTileManager.getTileEntityName(level, i, j);
					int left = bufferRect.left + (j-column) * MapSettings.TILE_WIDTH;
					int top = bufferRect.top + (i-row) * MapSettings.TILE_HEIGHT;
					int right = Math.min( left + MapSettings.TILE_WIDTH, bufferRect.right );
					int bottom = Math.min( top + MapSettings.TILE_HEIGHT, bufferRect.bottom );
					Rect src = new Rect(left,top,right,bottom);
					dst.right = right - left;
					dst.bottom = bottom - top;

					int progress = (int)( ( i * columnTotal + j ) * 100.0f / (rowTotal * columnTotal)  );
					publishProgress(new ProgressInfo(progress,100,"Saving...","Create map image"));
					
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
		
		
		@Override
		protected Void doInBackground(Void... params) {
			Uri uri = RenderMap.this.getIntent().getData();
			String mapName = MapUri.getMapName(uri);
			try {
				publishProgress(new ProgressInfo(0,100,"Loading map...","Create map image"));
				Model map = MapBuilder.loadModel(MapSettings.getMapFileName(mapName));
				recreate(map);
				MapSettings.clearScrollPosition(RenderMap.this, mapName);
			} catch (Exception e) {
				Log.e("aMetro","Failed creating map cache for " + mapName, e);
				mFailReason = e;
			}	
			return null;
		}
		
		@Override
		protected void onProgressUpdate(ProgressInfo... values) {
			ProgressInfo.ChangeProgress(
					values[0], 
					mProgressBar, 
					mProgressTitle, 
					mProgressText, 
					mProgressCounter,
					getString(R.string.template_progress_percent)
					);
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPreExecute() {
			setContentView(R.layout.render_map_progress);
			mProgressBar  = (ProgressBar) findViewById(R.id.render_map_progress_bar);
			mProgressTitle = (TextView)findViewById(R.id.render_map_progress_title);
			mProgressText = (TextView)findViewById(R.id.render_map_progress_text);
			mProgressCounter = (TextView)findViewById(R.id.render_map_progress_counter);
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			if(mFailReason!=null){
				Toast.makeText(RenderMap.this, "Map loading error: " + mFailReason.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				setResult(RESULT_CANCELED, getIntent());
				finish();
			}else{
				setResult(RESULT_OK, getIntent());
				finish();
			}
			super.onPostExecute(result);
		}
		
		@Override
		protected void onCancelled() {
			mIsCanceled = true;
			super.onCancelled();
		}
		
	}
	
	private RenderTask mRenderTask;

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.render_map_progress);
		mRenderTask = new RenderTask();
		mRenderTask.execute();
	}

	@Override
	protected void onStop() {
		if(mRenderTask!=null && mRenderTask.getStatus() != Status.FINISHED){
			mRenderTask.cancel(false);
		}
		super.onStop();
	}
	


}
