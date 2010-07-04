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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

public class SettingsActivity extends PreferenceActivity implements
		OnPreferenceClickListener {

	final Handler mHandler = new Handler();
	private boolean mPaymentLaunched = false;
	private PayPal mPayPalObject;

	private Preference mDonatePayPalMPL;
	private Preference mDonatePayPal;
	private Preference mDonateYandex;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		mDonatePayPalMPL = findPreference(getString(R.string.pref_donate_paypal_mpl_key));
		mDonatePayPal = findPreference(getString(R.string.pref_donate_paypal_key));
		mDonateYandex = findPreference(getString(R.string.pref_donate_yandex_key));

		mDonatePayPalMPL.setOnPreferenceClickListener(this);
		mDonatePayPal.setOnPreferenceClickListener(this);
		mDonateYandex.setOnPreferenceClickListener(this);

		initPayPal();
	}

	protected void onStop() {
		super.onStop();
	}

	public boolean onPreferenceClick(Preference preference) {
		if (preference == mDonatePayPalMPL) {
			invokePayPal();
		} else if (preference == mDonatePayPal) {
			invokeWebBrowser(getString(R.string.url_paypal));
		} else if (preference == mDonateYandex) {
			invokeWebBrowser(getString(R.string.url_yandex));
		}
		return false;
	}

	private void invokeWebBrowser(String uri) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(uri));
		startActivity(i);
	}

	private void invokePayPal() {
		if (mPaymentLaunched == false) {

			
			final EditText edit = new EditText(this);
			edit.setPadding(5, 5, 5, 5);
			FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			layout.setMargins(5, 5, 5, 5);
			edit.setLayoutParams(layout);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
				.setMessage(R.string.msg_enter_amount).setCancelable(false)
				.setView(edit)
				.setPositiveButton(getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							if (mPaymentLaunched == false) {
								
								mPaymentLaunched = true;
								PayPalPayment newPayment = new PayPalPayment();
								newPayment.setAmount( edit.getText().toString() );
								newPayment.setCurrency("USD");
								newPayment.setRecipient("ivan_1278274849_biz@gmail.com");
								newPayment.setMerchantName("aMetro Project");
								newPayment.setItemDescription("Donate to aMetro application developers");
								Intent payPalIntent = new Intent(SettingsActivity.this, PayPalActivity.class);
								payPalIntent.putExtra(PayPalActivity.EXTRA_PAYMENT_INFO, newPayment);
								SettingsActivity.this.startActivityForResult(payPalIntent, 1);
							}
						}
					}).setNegativeButton(getString(android.R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							// put your code here
							dialog.cancel();
						}
					});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();

		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mPaymentLaunched = false;
		if (requestCode != 1) {
			return;
		}

		switch (resultCode) {
		case RESULT_OK:
			String transactionID = data
					.getStringExtra(PayPalActivity.EXTRA_TRANSACTION_ID);
			this.paymentSucceeded(transactionID);
			break;
		case RESULT_CANCELED:
			this.paymentCanceled();
			break;
		case PayPalActivity.RESULT_FAILURE:
			String errorID = data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
			String errorMessage = data
					.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
			this.paymentFailed(errorID, errorMessage);
		}
	}

	public void paymentFailed(String errorID, String errorMessage) {
		Toast.makeText(this, getString(R.string.msg_payment_failed) + " " + errorMessage,
				Toast.LENGTH_SHORT).show();
	}

	public void paymentSucceeded(String transactionID) {
		Toast.makeText(this, getString(R.string.msg_payment_success), Toast.LENGTH_SHORT).show();
	}

	public void paymentCanceled() {
		Toast.makeText(this, getString(R.string.msg_payment_canceled), Toast.LENGTH_SHORT).show();
	}

	protected void initPayPal() {

		mDonatePayPalMPL.setEnabled(false);

		Thread t = new Thread() {
			public void run() {
				mPayPalObject = PayPal.initWithAppID(SettingsActivity.this
						.getBaseContext(), "APP-80W284485P519543T",
						PayPal.ENV_SANDBOX);
				mPayPalObject.setLang("en_US");
				mHandler.post(mUpdateResults);
			}
		};
		t.start();
	}

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			mDonatePayPalMPL.setEnabled(true);
		}
	};

}
