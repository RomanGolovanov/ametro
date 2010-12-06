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
package org.ametro.ui.adapters;

import org.ametro.model.SchemeView;
import org.ametro.model.route.RouteView;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

public class RouteListAdapter extends StationListAdapter {

	private static final int HEIGHT = 54; // in DIP
	private final int mHalfHeight;
	
	public RouteListAdapter(Context context, RouteView route, SchemeView map) {
		super(context, route.getStations(), route.getDelays(), map);
		final float scale = context.getResources().getDisplayMetrics().density;
		mHalfHeight = (int) (HEIGHT * scale + 0.5f)/2;
	}

	protected void setListItemView(ListItemWrapper wrapper, int position) {
		wrapper.StationImageShadow.setVisibility(View.VISIBLE);
		final ImageView img = wrapper.LineImage;
		img.setVisibility(View.VISIBLE);
		if (position == 0) {
			img.setPadding(img.getPaddingLeft(), mHalfHeight, img.getPaddingRight(), 0);
		} else if (position == (mFilteredStations.length - 1)) {
			img.setPadding(img.getPaddingLeft(), 0, img.getPaddingRight(), mHalfHeight);
		} else {
			img.setPadding(img.getPaddingLeft(), 0, img.getPaddingRight(), 0);
		}		
		super.setListItemView(wrapper, position);
	}
	
}
