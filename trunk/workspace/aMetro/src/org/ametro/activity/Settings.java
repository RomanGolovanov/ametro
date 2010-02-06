package org.ametro.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.ametro.R;

public class Settings extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

}
