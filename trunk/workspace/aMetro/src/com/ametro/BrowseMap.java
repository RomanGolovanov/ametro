package com.ametro;

import java.io.IOException;

import com.ametro.resources.FilePackage;
import com.ametro.resources.MapResource;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class BrowseMap extends Activity {

	private FilePackage mFilePackage;
	private MapResource mMap;

	private MapImageView mMapImageView;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			mFilePackage = new FilePackage("/sdcard/ametro/moscow.pmz");
			mMap = mFilePackage.getMapResource("Metro.map");
			mMapImageView = new MapImageView(getApplicationContext(), mMap);
			setContentView(mMapImageView); 		
			
			
		} catch (IOException e) {
			handleException(e);
		}


	}

	private void handleException(IOException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(e.getMessage())
		.setCancelable(false)
		.setPositiveButton("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}


}