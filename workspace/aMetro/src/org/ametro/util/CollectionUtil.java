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
package org.ametro.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CollectionUtil {

	public static HashSet<Integer> toHashSet(int[] values)
	{
		if(values==null){
			return null;
		}
		final HashSet<Integer> res = new  HashSet<Integer>();
		for(int state : values){
			res.add(state);
		}
		return res;
	}
	
	public 	static int[] toArray(List<Integer> src) {
		int[] res = new int[src.size()];
		for (int i = 0; i < src.size(); i++) {
			res[i] = src.get(i);
		}
		return res;
	}

	public static long[] toArray(ArrayList<Long> src) {
		long[] res = new long[src.size()];
		for (int i = 0; i < src.size(); i++) {
			res[i] = src.get(i);
		}
		return res;
	}

	public static String[] join(String[] left, String[] right) {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll( Arrays.asList(left) );
		list.addAll( Arrays.asList(right) );
		return (String[]) list.toArray(new String[list.size()]);
	}

	public static void ensureSize(ArrayList<?> collection, int size) {
		while(collection.size()<size){
			collection.add(null);
		}
	}

	public static int indexOf(String[] array, String value) {
		if(value == null)return -1;
		final int len = array.length;
		for(int i=0; i<len; i++){
			if(value.equals(array[i])){
				return i;
			}
		}
		return -1;
	}
	

}
