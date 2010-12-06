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

import java.util.Locale;

import org.ametro.R;
import org.ametro.app.Constants;
import org.ametro.ui.dialog.PaymentDetailsDialog;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class DonateActivity extends PreferenceActivity implements OnPreferenceClickListener {
	
	private static final int REQUEST_DONATE_DETAILS = 1;
	
	private Preference mDonateYandex;
	//private Preference mDonateWebMoney;
	private Preference mDonateMoneyBookers;
	private Preference mDonateAlertPay;
	private Preference mDonateQiwi;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_donate);
 
		mDonateAlertPay = findPreference(getString(R.string.pref_donate_alertpay_key));
		mDonateYandex = findPreference(getString(R.string.pref_donate_yandex_key));
		//mDonateWebMoney = findPreference(getString(R.string.pref_donate_wm_key));
		mDonateMoneyBookers = findPreference(getString(R.string.pref_donate_mb_key));
		mDonateQiwi = findPreference(getString(R.string.pref_donate_qiwi_key));

		mDonateAlertPay.setOnPreferenceClickListener(this);
		mDonateYandex.setOnPreferenceClickListener(this);
		//mDonateWebMoney.setOnPreferenceClickListener(this);
		mDonateMoneyBookers.setOnPreferenceClickListener(this);
		mDonateQiwi.setOnPreferenceClickListener(this);
	}

	protected void onStop() {
		super.onStop();
	}

	public boolean onPreferenceClick(Preference preference) {
		final Resources res = getResources();
		if (preference == mDonateAlertPay) {
			String[] codes = res.getStringArray(R.array.alertpay_currency_codes);
			String[] names = res.getStringArray(R.array.alertpay_currency_names);
			
			StringBuilder url = new StringBuilder();
			url.append("https://www.alertpay.com/PayProcess.aspx");
			url.append("?ap_currency="); url.append("%%CURRENCY%%");
			url.append("&ap_amount="); url.append("%%AMOUNT%%");
			url.append("&ap_itemname="); url.append("aMetro%20Project%20Support");
			url.append("&ap_purchasetype="); url.append("service");
			url.append("&ap_merchant="); url.append("roman.golovanov@gmail.com");
			
			invokePaymentDialog(url.toString(), codes, names, 1.0f);
		} 
		if (preference == mDonateYandex) {
			String[] codes = res.getStringArray(R.array.yandex_currency_codes);
			String[] names = res.getStringArray(R.array.yandex_currency_names);
			String url = "https://money.yandex.ru/charity.xml?to=41001667593841&CompanyName=aMetroProject&CompanyLink=http://www.ametro.org&CompanySum=%%AMOUNT%%";
			
			invokePaymentDialog(url, codes, names, 1.0f);
		}
//		if (preference == mDonateWebMoney) {
//			String language = Locale.getDefault().getLanguage();
//			
//			StringBuilder url = new StringBuilder();
//			if(language.equalsIgnoreCase("ru")){
//				url.append("https://light.webmoney.ru/pci.aspx");
//				url.append("?url="); url.append("http%3A//www.ametro.org/thanks");
//				url.append("&desc="); url.append("%D0%9F%D0%BE%D0%BC%D0%BE%D1%89%D1%8C%20%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D1%83%20aMetro%20");
//			}else{
//				url.append("https://light.wmtransfer.com/pci.aspx");
//				url.append("?url="); url.append("http%3A//www.ametro.org/thanks");
//				url.append("&desc="); url.append("aMetro%20Project%20Support");
//			}
//			url.append("&purse="); url.append("%%CURRENCY%%");
//			url.append("&amount="); url.append("%%AMOUNT%%");
//			url.append("&method="); url.append("GET");
//			url.append("&mode="); url.append("test");
//			
//			String[] codes = res.getStringArray(R.array.webmoney_currency_codes);
//			String[] names = res.getStringArray(R.array.webmoney_currency_names);
//			
//			invokePaymentDialog(url.toString(), codes, names, 1.0f);
//		}
		if (preference == mDonateMoneyBookers) {
			
			String language = Locale.getDefault().getLanguage();
			
			StringBuilder url = new StringBuilder();
			url.append("https://www.moneybookers.com/app/payment.pl");
			url.append("?amount="); url.append("%%AMOUNT%%");
			url.append("&currency="); url.append("%%CURRENCY%%");
			if(language.equalsIgnoreCase("ru")){
				url.append("&return_url="); url.append("http%3A//www.ametro.org/thanks");
				url.append("&language="); url.append("RU");
				url.append("&detail1_description="); url.append("%D0%9F%D0%BE%D0%BC%D0%BE%D1%89%D1%8C%20%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D1%83%20aMetro%20");
				url.append("&detail1_text="); url.append("%D0%9F%D0%BE%D0%BC%D0%BE%D1%89%D1%8C%20%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D1%83%20aMetro%20");
			}else{
				url.append("&return_url="); url.append("http%3A//www.ametro.org/thanks");
				url.append("&language="); url.append("EN");
				url.append("&detail1_description="); url.append("aMetro%20Project%20Support");
				url.append("&detail1_text="); url.append("aMetro%20Project%20Support");
			}
			url.append("&pay_to_email="); url.append("roman.golovanov@gmail.com");
			
			String[] codes = res.getStringArray(R.array.moneybookers_currency_codes);
			String[] names = res.getStringArray(R.array.moneybookers_currency_names);
			invokePaymentDialog(url.toString(), codes, names, 1.0f);
		}
		if(preference == mDonateQiwi){
			String url = "https://m.qiwi.ru/";
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(webIntent);			
		}
		return true;
	}

	private void invokePaymentDialog(String url, String[] codes, String[] names, float amount) {
		Intent i = new Intent(this, PaymentDetailsDialog.class);
		i.putExtra(PaymentDetailsDialog.EXTRA_ALLOW_DECIMAL_AMOUNT, true);
		i.putExtra(PaymentDetailsDialog.EXTRA_CURRENCY_CODES, codes );
		i.putExtra(PaymentDetailsDialog.EXTRA_CURRENCY_NAMES, names );
		i.putExtra(PaymentDetailsDialog.EXTRA_AMOUNT, amount);
		i.putExtra(PaymentDetailsDialog.EXTRA_CONTEXT, url);
		startActivityForResult(i, REQUEST_DONATE_DETAILS);
	}
	
	private static String applyTemplate(String template, String currency, float amount){
		return template.replaceAll("%%AMOUNT%%", Float.toString(amount) ).replaceAll("%%CURRENCY%%", currency);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_DONATE_DETAILS:
			if(resultCode == RESULT_OK){
				String currency = data.getStringExtra(PaymentDetailsDialog.EXTRA_CURRENCY);
				float amount = data.getFloatExtra(PaymentDetailsDialog.EXTRA_AMOUNT, 0.0f);
				String template = data.getStringExtra(PaymentDetailsDialog.EXTRA_CONTEXT);
				String url = applyTemplate(template, currency, amount);
				Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.DEBUG)){
					Log.d(Constants.LOG_TAG_MAIN, "Start payment with currency: " + currency + ", amount: " + amount);
					Log.d(Constants.LOG_TAG_MAIN, "URL: " + url);
				}
				startActivity(webIntent);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
