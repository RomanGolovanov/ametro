package org.ametro.util;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class BarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar mBar;
	private TextView mMessageText, mValueText;

	private String mPostfix;
	private int mDefault = 0;
	private int mMax = 100;
	private int mValue = 0;

	public BarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPostfix = attrs.getAttributeValue(androidns, "postfix");
		mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		mMax = attrs.getAttributeIntValue(androidns, "max", 100);
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		String valueText = String.valueOf(value);
		mValueText.setText(mPostfix == null ? valueText : valueText + mPostfix);
		if (shouldPersist()){
			persistInt(value);
		}
		callChangeListener(new Integer(value));
	}

	public void onStartTrackingTouch(SeekBar seek) {
	}

	public void onStopTrackingTouch(SeekBar seek) {
	}
	
	protected View onCreateDialogView() {
		LinearLayout container = new LinearLayout(getContext());
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(10, 5, 10, 5);

		mMessageText = new TextView(getContext());
		mMessageText.setText(getSummary());
		container.addView(mMessageText);

		mValueText = new TextView(getContext());
		mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
		container.addView(mValueText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		mBar = new SeekBar(getContext());
		mBar.setOnSeekBarChangeListener(this);
		mBar.setPadding(5, 0, 5, 0);
		container.addView(mBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		

		if (shouldPersist()){
			mValue = getPersistedInt(mDefault);
		}

		mBar.setMax(mMax);
		mBar.setProgress(mValue);
		return container;
	}

	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		mBar.setMax(mMax);
		mBar.setProgress(mValue);
	}

	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}		
}
