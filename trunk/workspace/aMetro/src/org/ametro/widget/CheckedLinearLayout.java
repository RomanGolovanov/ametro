package org.ametro.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

public class CheckedLinearLayout extends LinearLayout implements Checkable {

	private CheckedTextView mText;
	
	public CheckedLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckedLinearLayout(Context context) {
		super(context);
	}

	public boolean isChecked() {
		return getTextView().isChecked();
	}

	public void setChecked(boolean checked) {
		CheckedTextView view = getTextView();
		if(view.isEnabled()){
			view.setChecked(checked);
		}
	}

	public void toggle() {
		CheckedTextView view = getTextView();
		if(view.isEnabled()){
			view.toggle();
		}
	}

	private CheckedTextView getTextView(){
		if(mText==null){
			mText = (CheckedTextView)findViewById(android.R.id.text1);
		}
		return mText;
	}

}
