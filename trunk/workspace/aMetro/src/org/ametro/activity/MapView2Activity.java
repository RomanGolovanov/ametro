package org.ametro.activity;

import org.ametro.GlobalSettings;
import org.ametro.model.MapView;
import org.ametro.model.Model;
import org.ametro.model.storage.ModelBuilder;
import org.ametro.widget.MapViewBasic;
import org.ametro.widget.MapViewMultitouch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MapView2Activity extends Activity {

	static class MultitouchWrapper
	{
		public static MapViewBasic getMapView(Context context, Model model, MapView scheme)
		{
			return new MapViewMultitouch(context, model, scheme);
		}
	}

	class LoadMapTask extends AsyncTask<Void, Void, Model>
	{

		
		
		private ProgressDialog dialog;
		private MapView mapView;
		
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MapView2Activity.this, "Map Loading", "Please wait for a moment....", true);
			super.onPreExecute();
		}
		
		protected Model doInBackground(Void... params) {
			GlobalSettings.MapPath path = GlobalSettings.getCurrentMap(MapView2Activity.this);
			//MapPath path = new MapPath("/sdcard/ametro/maps/peterburg.pmz.ametro,Metro.map");
			Model m = ModelBuilder.loadModel(path.FilePath);
			mapView = m.loadView(path.ViewName);
			return m;
		}
		
		protected void onPostExecute(Model model) {
			dialog.hide();
			if(model!=null){
				boolean hasMultiTouch = Integer.parseInt(Build.VERSION.SDK) >= 5;
				MapViewBasic view;
				if(hasMultiTouch){
					view = MultitouchWrapper.getMapView(MapView2Activity.this, model, mapView);
				}else{
					view = new MapViewBasic(MapView2Activity.this, model, mapView);
				}
				mContent.addView(view);
			}else{
				Toast.makeText(MapView2Activity.this, "Cannot load map", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(model);
		}
		
	}

	FrameLayout mContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContent = new FrameLayout(this);
		setContentView(mContent);
		(new LoadMapTask()).execute();
	}

}