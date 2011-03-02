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

package org.ametro.ui.view;

import static org.ametro.catalog.CatalogMapState.IMPORT_NEED_TO_UPDATE;
import static org.ametro.catalog.CatalogMapState.IMPORT_UPDATE;

import org.ametro.R;
import org.ametro.catalog.CatalogMapState;
import org.ametro.util.StringUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TextStripView extends LinearLayout {

	public abstract static class Element {
		protected View mContent;

		/* package */Element(Context context, int res) {
			mContent = LayoutInflater.from(context).inflate(res, null);
		}

		public View getContent() {
			return mContent;
		}
	}

	public static class HeaderView extends Element {
		private TextView mLeft;
		private TextView mRight;

		public HeaderView setTextLeft(CharSequence text) {
			mLeft.setText(text);
			return this;
		}

		public void setTextLeftColor(int color) {
			mLeft.setTextColor(color);
		}

		public HeaderView setTextRight(CharSequence text) {
			mRight.setText(text);
			return this;
		}

		public void setTextRightColor(int color) {
			mRight.setTextColor(color);
		}

		/* package */HeaderView(Context context) {
			super(context, R.layout.map_details_header);
			mLeft = (TextView) mContent.findViewById(R.id.text_left);
			mRight = (TextView) mContent.findViewById(R.id.text_right);
		}

	}

	public static class TextBlockView extends Element {
		private TextView mText;

		public void setText(CharSequence text) {
			mText.setText(text);
		}

		/* package */TextBlockView(Context context) {
			super(context, R.layout.map_details_text);
			mText = (TextView) mContent.findViewById(R.id.text);
		}
		
		public TextView getText() {
			return mText;
		}		
	}

	public static class WidgetBlockView extends Element {
		protected FrameLayout mContainer;

		/* package */WidgetBlockView(Context context) {
			super(context, R.layout.map_details_widget);
			mContainer = (FrameLayout) mContent.findViewById(R.id.content);
		}
	}

	public static class OnlineWidgetView extends WidgetBlockView {

		protected TextView mSize;
		protected TextView mVersion;
		protected Button mDownloadButton;
		protected Button mUpdateButton;
		protected Button mCancelButton;
		protected ProgressBar mProgressBar;

		public void setSize(long size) {
			mSize.setText(StringUtil.formatFileSize(size, 0));
		}

		public void setVersion(String version) {
			mVersion.setText(version);
		}

		public Button getDownloadButton(){
			return mDownloadButton;
		}
		
		public Button getUpdateButton(){
			return mUpdateButton;
		}
		
		public Button getCancelButton(){
			return mCancelButton;
		}
		
		public void setVisibility(int state) {
			mDownloadButton.setVisibility(state == CatalogMapState.DOWNLOAD ? View.VISIBLE : View.GONE);
			mUpdateButton.setVisibility(state == CatalogMapState.UPDATE || state == CatalogMapState.NEED_TO_UPDATE ? View.VISIBLE : View.GONE);
			mCancelButton.setVisibility(
					/*state == CatalogMapState.DOWNLOADING ||*/ 
					state == CatalogMapState.DOWNLOAD_PENDING ? View.VISIBLE : 
						(state == CatalogMapState.DOWNLOADING ? View.INVISIBLE : View.GONE)
				);
			mCancelButton.setEnabled(state == CatalogMapState.DOWNLOAD_PENDING);
			mProgressBar.setVisibility(state == CatalogMapState.DOWNLOADING ? View.VISIBLE : View.GONE);
			if(state == CatalogMapState.DOWNLOADING){
				setIndeterminateProgress();
			}			
		}

		public void setProgress(int progress, int total){
			mProgressBar.setIndeterminate(false);
			mProgressBar.setMax(total);
			mProgressBar.setProgress(progress);
		}
		
		public void setIndeterminateProgress(){
			mProgressBar.setIndeterminate(true);
		}
		
		
		/* package */OnlineWidgetView(Context context) {
			super(context);
			mContainer.addView(LayoutInflater.from(context).inflate(
					R.layout.map_details_online_widget, null));
			mSize = (TextView) mContainer.findViewById(R.id.size);
			mVersion = (TextView) mContainer.findViewById(R.id.version);
			mDownloadButton = (Button) mContainer
					.findViewById(R.id.btn_download);
			mUpdateButton = (Button) mContainer.findViewById(R.id.btn_update);
			mCancelButton = (Button) mContainer.findViewById(R.id.btn_cancel);
			mProgressBar = (ProgressBar) mContainer
					.findViewById(R.id.progressbar);
		}
	}

	public static class ImportWidgetView extends WidgetBlockView {

		protected TextView mSize;
		protected TextView mVersion;
		protected Button mImportButton;
		protected Button mUpdateButton;
		protected Button mCancelButton;
		protected ProgressBar mProgressBar;

		public void setSize(long size) {
			mSize.setText(StringUtil.formatFileSize(size, 0));
		}

		public void setVersion(String version) {
			mVersion.setText(version);
		}

		public Button getImportButton(){
			return mImportButton;
		}
		
		public Button getUpdateButton(){
			return mUpdateButton;
		}
		
		public Button getCancelButton(){
			return mCancelButton;
		}
		
		public void setVisibility(int state) {
			mImportButton.setVisibility(state == CatalogMapState.IMPORT ? View.VISIBLE : View.GONE);
			mUpdateButton.setVisibility(state == IMPORT_UPDATE || state == IMPORT_NEED_TO_UPDATE ? View.VISIBLE : View.GONE);
			mCancelButton.setVisibility(state == CatalogMapState.IMPORTING || state == CatalogMapState.IMPORT_PENDING ? View.VISIBLE : View.GONE);
			mCancelButton.setEnabled(state == CatalogMapState.IMPORT_PENDING);
			mProgressBar.setVisibility(state == CatalogMapState.IMPORTING ? View.VISIBLE : View.GONE);
			if(state == CatalogMapState.IMPORTING){
				setIndeterminateProgress();
			}
		}

		public void setProgress(int progress, int total){
			mProgressBar.setIndeterminate(false);
			mProgressBar.setMax(total);
			mProgressBar.setProgress(progress);
		}
		
		public void setIndeterminateProgress(){
			mProgressBar.setIndeterminate(true);
		}
		
		/* package */ImportWidgetView(Context context) {
			super(context);
			mContainer.addView(LayoutInflater.from(context).inflate(
					R.layout.map_details_import_widget, null));
			mSize = (TextView) mContainer.findViewById(R.id.size);
			mVersion = (TextView) mContainer.findViewById(R.id.version);
			mImportButton = (Button) mContainer.findViewById(R.id.btn_import);
			mUpdateButton = (Button) mContainer.findViewById(R.id.btn_update);
			mCancelButton = (Button) mContainer.findViewById(R.id.btn_cancel);
			mProgressBar = (ProgressBar) mContainer
					.findViewById(R.id.progressbar);
		}
	}

	public static class TransportWidgetView extends WidgetBlockView {
		private TextView mText;
		private ImageView mImage;

		public TransportWidgetView setText(CharSequence text) {
			mText.setText(text);
			return this;
		}

		public TransportWidgetView setImageDrawable(Drawable drawable) {
			mImage.setImageDrawable(drawable);
			return this;
		}

		/* package */TransportWidgetView(Context context) {
			super(context);
			mContainer.addView(LayoutInflater.from(context).inflate(
					R.layout.map_details_transport_widget, null));
			mText = (TextView) mContainer.findViewById(R.id.text);
			mImage = (ImageView) mContainer.findViewById(R.id.image);
		}
	}

	public TextStripView(Context context) {
		super(context);
		setOrientation(VERTICAL);
	}

	public TextStripView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
	}

	public void removeAll() {
		removeAllViews();
	}

	public TextBlockView createText() {
		TextBlockView v = new TextBlockView(getContext());
		addView(v.getContent());
		return v;
	}

	public HeaderView createHeader() {
		HeaderView v = new HeaderView(getContext());
		addView(v.getContent());
		return v;
	}

	public OnlineWidgetView createOnlineWidget() {
		OnlineWidgetView v = new OnlineWidgetView(getContext());
		addView(v.getContent());
		return v;
	}

	public ImportWidgetView createImportWidget() {
		ImportWidgetView v = new ImportWidgetView(getContext());
		addView(v.getContent());
		return v;
	}

	public TransportWidgetView createTransportWidget() {
		TransportWidgetView v = new TransportWidgetView(getContext());
		addView(v.getContent());
		return v;

	}

}
