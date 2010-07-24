/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.activity;

import org.ametro.R;
import org.ametro.dialog.DownloadIconsDialog;
import org.ametro.dialog.EULADialog;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {

	private Preference mDonate;
	private Preference mLicense;
	private Preference mRefreshIconPack;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		mDonate = findPreference(getString(R.string.pref_section_donate_key));
		mLicense = findPreference(getString(R.string.pref_section_license_key));
		mRefreshIconPack = findPreference(getString(R.string.pref_refresh_country_icons_key));

		mDonate.setOnPreferenceClickListener(this);
		mLicense.setOnPreferenceClickListener(this);
		mRefreshIconPack.setOnPreferenceClickListener(this);
	}

	protected void onStop() {
		super.onStop();
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference == mRefreshIconPack){
			DownloadIconsDialog dialog = new DownloadIconsDialog(this,false);
			dialog.show();
		}
		if (preference == mDonate) {
			startActivity(new Intent(this, DonateActivity.class));
		}
		if (preference == mLicense) {
			EULADialog.show(this);
		}
		return false;
	}

	
	
}
