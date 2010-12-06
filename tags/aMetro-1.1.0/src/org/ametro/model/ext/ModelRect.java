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
package org.ametro.model.ext;

public class ModelRect {

	public int left;
	public int right;
	public int top;
	public int bottom;
	
	public int width() { return right - left; }
	public int height() { return bottom - top; }
	
	public ModelRect(){
	}
	
	public ModelRect(int left, int top, int right, int bottom){
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public void offset(int dx, int dy) {
		left += dx;
		right += dx;
		top += dy;
		bottom += dy;
	}
	
	public boolean isZero() {
		return left==0 && right==0 && top==0 && bottom ==0;
	}
	
	public boolean contains(int x, int y) {
		return x >= left && x <= right && y >= top && y <= bottom;
	}
	
}
