package com.ametro.activity;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.content.res.Configuration;
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
import com.ametro.model.Model;
import com.ametro.model.ModelBuilder;
import com.ametro.model.ModelDescription;
import com.ametro.model.Tile;
import com.ametro.model.TileOutputStream;
import com.ametro.model.ModelRenderer.RenderIterator;

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
			ModelDescription description = new ModelDescription(model);
			description.setRenderVersion(MapSettings.getRenderVersion());
			
			TileOutputStream container = null;
			try{
				Date startTimestamp = new Date();
				container = new TileOutputStream(file);
				container.write(description);
				RenderIterator iterator = new RenderIterator(model);
				while(iterator.hasNext() && !mIsCanceled){
					int progress = 100 * iterator.position() / iterator.size();
					publishProgress(new ProgressInfo( progress, 100 ,"Rendering...","Create map image"));
					Tile tile = iterator.next();
					container.write(tile);
				}
				container.close();
				container = null;
				if(!mIsCanceled){
					Log.i("aMetro","Commit model cache");
					file.renameTo(new File(MapSettings.getCacheFileName(mapName)));
				}else{
					Log.i("aMetro","Rollback model cache due cancelation");
					file.delete();
				}
				Log.i("aMetro", String.format("Model '%s' render time is %sms", model.getMapName(), Long.toString((new Date().getTime() - startTimestamp.getTime())) ));
			}catch(Exception ex){
				Log.i("aMetro","Rollback model cache due exception");
				file.delete();
			}finally{
				if(container!=null){
					try{ container.close(); }catch(Exception ex){}
				}
			}
			
		}
				
		@Override
		protected Void doInBackground(Void... params) {
			Uri uri = RenderMap.this.getIntent().getData();
			String mapName = MapUri.getMapName(uri);
			try {
				publishProgress(new ProgressInfo(0,100,"Loading map...","Create map image"));
				Model map = ModelBuilder.loadModel(MapSettings.getMapFileName(mapName));
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
