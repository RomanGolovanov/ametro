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

package org.ametro.catalog;

public class CatalogMapPairEx extends CatalogMapPair {

	private boolean mIsVisible;
	private boolean mIsCheckable;
	private boolean mIsChecked;

	public CatalogMapPairEx(CatalogMap mLocal, CatalogMap mRemote, int preffered, boolean checkable, boolean checked, boolean visible) {
		super(mLocal, mRemote, preffered);
		this.mIsCheckable = checkable;
		this.mIsChecked = checked;
		this.mIsVisible = visible;
	}

	public CatalogMapPairEx(CatalogMapPair src, boolean checkable, boolean checked, boolean visible) {
		super(src.mLocal, src.mRemote, src.mPreffered);
		this.mIsCheckable = checkable;
		this.mIsChecked = checked;
		this.mIsVisible = visible;
	}

	public boolean isVisible() {
		return mIsVisible;
	}
	
	public boolean isCheckable() {
		return mIsCheckable;
	}

	public boolean isChecked() {
		return mIsChecked;
	}

	public void setChecked(boolean checked) {
		mIsChecked = checked;
	}

}
