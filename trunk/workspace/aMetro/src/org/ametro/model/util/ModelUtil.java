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
package org.ametro.model.util;

import java.util.ArrayList;

import org.ametro.model.StationView;
import org.ametro.model.ext.ModelPoint;
import org.ametro.model.ext.ModelRect;
import org.ametro.util.StringUtil;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;


public class ModelUtil {

	public static class DelaysString {

		private String mText;
		// private String[] mParts;
		private int mPos;
		private int mLen;

		public DelaysString(String text) {
			// text = text.replaceAll("\\(","");
			// text = text.replaceAll("\\)","");
			// mParts = text.split(",");
			mText = text;
			mLen = text != null ? mText.length() : 0;
			mPos = 0;
		}

		public boolean beginBracket() {
			return mText != null && mPos < mLen && mText.charAt(mPos) == '(';
		}

		private String nextBlock() {
			if (mText == null)
				return null;
			int nextComma = mText.indexOf(",", beginBracket() ? mText.indexOf(
					")", mPos) : mPos);
			String block = nextComma != -1 ? mText.substring(mPos, nextComma)
					: mText.substring(mPos);
			mPos = nextComma != -1 ? nextComma + 1 : mLen;
			return block;
		}

		public Integer next() {
			return StringUtil.parseNullableDelay(nextBlock());
		}

		public Integer[] nextBracket() {
			if (mText == null)
				return null;
			String block = nextBlock();
			return StringUtil.parseDelayArray(block.substring(1, block.length() - 1));
		}
	}

	public static class StationsString {
		private String mText;
		private String mDelimeters;
		private int mPos;
		private int mLen;
		private String mNextDelimeter;

		public String getNextDelimeter() {
			return mNextDelimeter;
		}

		public StationsString(String text) {
			mText = text;
			mLen = text.length();
			mDelimeters = ",()";
			reset();
		}

		public void reset(){
			mPos = 0;
			skipToContent();
		}

		public boolean hasNext() {
			int saved = mPos;
			skipToContent();
			boolean result = mPos != mLen;
			mPos = saved;
			return result;
		}

		public String next() {
			skipToContent();
			if (mPos == mLen) {
				return "";
			}
			int pos = mPos;
			String symbol = null;
			boolean quotes = false;
			while (pos < mLen
					&& (!mDelimeters.contains(symbol = mText.substring(pos,
							pos + 1)) || quotes)) {
				if ("\"".equals(symbol)) {
					quotes = !quotes;
				}
				pos++;
			}
			int end = symbol == null ? pos - 1 : pos;
			mNextDelimeter = symbol;
			String text = mText.substring(mPos, end);
			mPos = end;
			if (text.startsWith("\"") && text.endsWith("\""))
				text = text.substring(1, text.length() - 1);
			return text;
		}

		private void skipToContent() {
			String symbol;
			String symbolNext = (mPos < mLen) ? mText.substring(mPos, mPos + 1)
					: null;
			while (mPos < mLen && mDelimeters.contains(symbol = symbolNext)) {
				if ("(".equals(symbol)) {
					mPos++;
					return;
				} else if (")".equals(symbol)) {
				}
				mPos++;
				symbolNext = (mPos < mLen) ? mText.substring(mPos, mPos + 1)
						: null;
				if (",".equals(symbol) && !"(".equals(symbolNext))
					return;
			}
		}
	}

	public static ModelRect getDimensions(StationView stations[]) {
		int xmin = Integer.MAX_VALUE;
		int ymin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymax = Integer.MIN_VALUE;

		for (StationView station : stations) {
			ModelPoint p = station.stationPoint;
			if (p != null) {
				if (xmin > p.x)
					xmin = p.x;
				if (ymin > p.y)
					ymin = p.y;

				if (xmax < p.x)
					xmax = p.x;
				if (ymax < p.y)
					ymax = p.y;
			}
			ModelRect r = station.stationNameRect;
			if (r != null) {
				if (xmin > r.left)
					xmin = r.left;
				if (ymin > r.top)
					ymin = r.top;
				if (xmin > r.right)
					xmin = r.right;
				if (ymin > r.bottom)
					ymin = r.bottom;

				if (xmax < r.left)
					xmax = r.left;
				if (ymax < r.top)
					ymax = r.top;
				if (xmax < r.right)
					xmax = r.right;
				if (ymax < r.bottom)
					ymax = r.bottom;
			}
		}
		return new ModelRect(xmin, ymin, xmax, ymax);
	}

	public static Rect toRect(ModelRect r) {
		if(r==null) return null;
		return new Rect(r.left,r.top,r.right,r.bottom);
	}

	public static RectF toRectF(ModelRect r) {
		if(r==null) return null;
		return new RectF(r.left,r.top,r.right,r.bottom);
	}

	public static Point toPoint(ModelPoint p) {
		if(p==null) return null;
		return new Point(p.x,p.y);
	}

	public static PointF toPointF(ModelPoint p) {
		if(p==null) return null;
		return new PointF(p.x,p.y);
	}

	public static Rect computeBoundingBox(final ArrayList<StationView> stations){ 
        Rect box = null;
        for(StationView station : stations){
        	final Point p = toPoint(station.stationPoint);
        	final Rect r = toRect(station.stationNameRect);
        	if(p==null && r==null){
        		continue;
        	}
        	if(box==null){
        		if(r!=null){
        			box = r;
        		}else{
        			box = new Rect(p.x, p.y, p.x, p.y);
        		}
        	}
        	if(p!=null){
        		box.union(p.x, p.y);
        	}
        	if(r!=null){
        		box.union(r);
        	}
        	
        }
        return box;
	}


}
