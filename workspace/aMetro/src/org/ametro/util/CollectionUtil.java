package org.ametro.util;

import java.util.ArrayList;
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
	

}
