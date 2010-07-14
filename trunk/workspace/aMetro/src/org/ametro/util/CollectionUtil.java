package org.ametro.util;

import java.util.HashSet;

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
	
}
