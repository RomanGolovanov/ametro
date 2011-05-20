/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package org.ametro.ui;

import java.util.Date;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.GlobalSettings;
import org.ametro.ui.dialog.DownloadIconsDialog;
import org.ametro.ui.dialog.EULADialog;
import org.ametro.ui.dialog.InfoDialog;
import org.ametro.util.CollectionUtil;
import org.ametro.util.DateUtil;
import org.ametro.util.FileUtil;
import org.ametro.util.StringUtil;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {


	//private Preference mDonate;
	private Preference mLicense;
	private Preference mRefreshIconPack;
	private Preference mAutoUpdateEnabled;

	private Preference mAutoUpdatePeriod;
	private Preference mAutoUpdateNetworks;

	private boolean mEnableAutoUpdateBeforeChange;
	private long mAutoUpdatePeriodValue;

	private CheckBoxPreference mLocationSearchEnabled;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEnableAutoUpdateBeforeChange = GlobalSettings
				.isAutoUpdateIndexEnabled(this);
		mAutoUpdatePeriodValue = GlobalSettings.getUpdatePeriod(this);

		addPreferencesFromResource(R.xml.settings);
		//mDonate = findPreference(getString(R.string.pref_section_donate_key));
		mLicense = findPreference(getString(R.string.pref_section_license_key));
		mRefreshIconPack = findPreference(getString(R.string.pref_refresh_country_icons_key));
		mAutoUpdateEnabled = findPreference(getString(R.string.pref_auto_update_map_index_key));
		mAutoUpdatePeriod = findPreference(getString(R.string.pref_auto_update_period_key));
		mAutoUpdateNetworks = findPreference(getString(R.string.pref_auto_update_networks_key));

		mLocationSearchEnabled = (CheckBoxPreference) findPreference(getString(R.string.pref_auto_locate_key));

		//mDonate.setOnPreferenceClickListener(this);
		mLicense.setOnPreferenceClickListener(this);
		mRefreshIconPack.setOnPreferenceClickListener(this);

		mLocationSearchEnabled.setOnPreferenceChangeListener(this);
		mAutoUpdatePeriod.setOnPreferenceChangeListener(this);
		mAutoUpdateNetworks.setOnPreferenceChangeListener(this);

		if (mEnableAutoUpdateBeforeChange) {
			mAutoUpdateEnabled.setSummary(getString(R.string.msg_last_update)
					+ " "
					+ DateUtil.getDateTime(new Date(GlobalSettings
							.getUpdateDate(this))));
		}

		updateDescription(mAutoUpdatePeriod,
				R.string.pref_auto_update_period_key,
				R.string.pref_auto_update_period_description,
				R.array.pref_auto_update_period_texts,
				R.array.pref_auto_update_period_values, null);

		updateDescription(mAutoUpdateNetworks,
				R.string.pref_auto_update_networks_key,
				R.string.pref_auto_update_networks_description,
				R.array.pref_auto_update_networks_texts,
				R.array.pref_auto_update_networks_values, null);
	}

	private void updateDescription(Preference pref, int prefId,
			int prefSummaryId, int textsId, int valuesId, String value) {
		final Resources res = getResources();
		String[] names = res.getStringArray(textsId);
		String[] keys = res.getStringArray(valuesId);
		if (value == null) {
			value = PreferenceManager.getDefaultSharedPreferences(this)
					.getString(res.getString(prefId), null);
		}
		int index = CollectionUtil.indexOf(keys, value);
		if (index != -1) {
			String baseSummary = getString(prefSummaryId);
			pref.setSummary((StringUtil.isNullOrEmpty(baseSummary) ? ""
					: baseSummary + ", ") + names[index]);
		}

	}

	protected void onStop() {
		super.onStop();
	}

	protected void onPause() {
		if (mEnableAutoUpdateBeforeChange != GlobalSettings
				.isAutoUpdateIndexEnabled(this)
				|| mAutoUpdatePeriodValue != GlobalSettings
						.getUpdatePeriod(this)) {
			ApplicationEx.getInstance().invalidateAutoUpdate();
		}
		super.onPause();
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference == mRefreshIconPack) {
			DownloadIconsDialog dialog = new DownloadIconsDialog(this, false);
			dialog.show();
		}
		//if (preference == mDonate) {
		//	startActivity(new Intent(this, DonateActivity.class));
		//}
		if (preference == mLicense) {
			EULADialog.show(this);
		}
		return false;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mLocationSearchEnabled) {
			boolean value = (Boolean) newValue;
			if (value) {
				try {
					String text = FileUtil.writeToString(getResources()
							.openRawResource(R.raw.location_warning));
					InfoDialog.showInfoDialog(this,
							getString(R.string.title_info), text,
							android.R.drawable.ic_dialog_info);
				} catch (Exception e) {
				}
			}
		}
		if (preference == mAutoUpdatePeriod) {
			String value = (String) newValue;
			updateDescription(mAutoUpdatePeriod,
					R.string.pref_auto_update_period_key,
					R.string.pref_auto_update_period_description,
					R.array.pref_auto_update_period_texts,
					R.array.pref_auto_update_period_values, value);
		}
		if (preference == mAutoUpdateNetworks) {
			String value = (String) newValue;
			updateDescription(mAutoUpdateNetworks,
					R.string.pref_auto_update_networks_key,
					R.string.pref_auto_update_networks_description,
					R.array.pref_auto_update_networks_texts,
					R.array.pref_auto_update_networks_values, value);
		}
		return true;
	}

}
