package org.ametro.widget;

import org.ametro.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextStripView extends LinearLayout {

	private LayoutInflater mInflater;
	
	public TextStripView(Context context) {
		super(context);
		setOrientation(VERTICAL);
		mInflater = LayoutInflater.from(context);
	}
	
	public TextStripView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		mInflater = LayoutInflater.from(context);
	}

	public void removeAll(){
		removeAllViews();
	}
	
	public void addText(CharSequence text){
		View v = (View)mInflater.inflate(R.layout.map_details_text, null);
		TextView t = (TextView)v.findViewById(R.id.text);
		t.setText(text);
		addView(v);
	}

	public void addHeader(CharSequence text){
		View v = (View)mInflater.inflate(R.layout.map_details_header, null);
		TextView t = (TextView)v.findViewById(R.id.text);
		t.setText(text);
		addView(v);
	}
	
	public void addWidgetBlock(View view){
		View v = (View)mInflater.inflate(R.layout.map_details_widget, null);
		FrameLayout l = (FrameLayout)v.findViewById(R.id.content);
		l.addView(view);
		addView(v);
	}	

}
