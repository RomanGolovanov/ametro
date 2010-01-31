package com.ametro.activity;

import com.ametro.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class About extends Activity {
	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

        try {
			PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			String versionName = info.versionName;
			TextView versionText = (TextView)findViewById(R.id.about_version_text);
			versionText.setText(
				String.format(
						"%s %s",
						getString(R.string.app_name),
						versionName
						));
		} catch (NameNotFoundException e) {
			finish();
		}
        
	}
}
