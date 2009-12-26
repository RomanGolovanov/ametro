package com.ametro.model;

import android.graphics.Point;
import android.graphics.Rect;


class Helpers {

//	public final double angle(double x1, double y1, double x2, double y2) {
	//
//				double dx = x2-x1;
//				double dy = y2-y2;
//				double angle = 0.0d;
	//
//				if (dx == 0.0) {
//					if(dy == 0.0)     angle = 0.0;
//					else if(dy > 0.0) angle = Math.PI / 2.0;
//					else              angle = (Math.PI * 3.0) / 2.0;
//				}
//				else if(dy == 0.0) {
//					if(dx > 0.0)      angle = 0.0;
//					else              angle = Math.PI;
//				}
//				else {
//					if(dx < 0.0)      angle = Math.atan(dy/dx) + Math.PI;
//					else if(dy < 0.0) angle = Math.atan(dy/dx) + (2*Math.PI);
//					else              angle = Math.atan(dy/dx);
//				}
//				return (angle * 180) / Math.PI;
//			}	

	
	public String convertCommas(String str)
	{
		StringBuilder sb = new StringBuilder(str);
		boolean f = false;
		for(int i = 0; i < str.length();i++){
			char ch = str.charAt(i);
			if(ch == '('){
				f = true;
			}else if (ch == ')'){
				f = false;
			}else if(f && ch == ','){
				sb.setCharAt(i, ';');
			}
		}
		return sb.toString();
	}
	
	public static Double parseNullableDouble(String text){
		if(text != null && !text.equals("")){
			try{
			return Double.parseDouble(text);
			} catch(NumberFormatException ex){}
		}
		return null;
	}
	

	static boolean[] resizeArray(boolean[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			boolean[] newArray = new boolean[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new boolean[newSize];
		}
	}	

	static int[] resizeArray(int[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			int[] newArray = new int[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new int[newSize];
		}
	}	

	static Integer[] resizeArray(Integer[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			Integer[] newArray = new Integer[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new Integer[newSize];
		}
	}	
	
	static String[] resizeArray(String[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			String[] newArray = new String[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new String[newSize];
		}
	}	

	static Rect[] resizeArray(Rect[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			Rect[] newArray = new Rect[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new Rect[newSize];
		}
	}	

	static Point[] resizeArray(Point[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			Point[] newArray = new Point[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new Point[newSize];
		}
	}	


	static Double[] resizeArray(Double[] oldArray, int newSize) {
		if(oldArray!=null){
			int oldSize = oldArray.length;
			Double[] newArray = new Double[newSize];
			int preserveLength = Math.min(oldSize,newSize);
			if (preserveLength > 0)
				System.arraycopy (oldArray,0,newArray,0,preserveLength);
			return newArray; 
		}else{
			return new Double[newSize];
		}
	}	

	

	static boolean[][] resizeArray(boolean[][] oldArray, int newSizeRow, int newSizeColumn) {
		if(oldArray!=null){
			boolean[][] newArray = (boolean[][])resizeGenericArray(oldArray,newSizeRow);
			for (int i=0; i<newArray.length; i++) {
				if (newArray[i] == null){
					newArray[i] = new boolean[newSizeColumn];
				}else {
					newArray[i] = (boolean[])resizeArray(newArray[i],newSizeColumn); 
				}	
			}
			return newArray;
		}else{
			return new boolean[newSizeRow][newSizeColumn];
		}
	}	
	
	static int[][] resizeArray(int[][] oldArray, int newSizeRow, int newSizeColumn) {
		if(oldArray!=null){
			int[][] newArray = (int[][])resizeGenericArray(oldArray,newSizeRow);
			for (int i=0; i<newArray.length; i++) {
				if (newArray[i] == null){
					newArray[i] = new int[newSizeColumn];
				}else {
					newArray[i] = (int[])resizeArray(newArray[i],newSizeColumn); 
				}	
			}
			return newArray;
		}else{
			return new int[newSizeRow][newSizeColumn];
		}
	}	
	
	static Point[][] resizeArray(Point[][] oldArray, int newSizeRow, int newSizeColumn) {
		if(oldArray!=null){
			Point[][] newArray = (Point[][])resizeGenericArray(oldArray,newSizeRow);
			for (int i=0; i<newArray.length; i++) {
				if (newArray[i] == null){
					newArray[i] = new Point[newSizeColumn];
				}else {
					newArray[i] = (Point[])resizeArray(newArray[i],newSizeColumn); 
				}	
			}
			return newArray;
		}else{
			return new Point[newSizeRow][newSizeColumn];
		}
	}	
	
	static Double[][] resizeArray(Double[][] oldArray, int newSizeRow, int newSizeColumn) {
		if(oldArray!=null){
			Double[][] newArray = (Double[][])resizeGenericArray(oldArray,newSizeRow);
			for (int i=0; i<newArray.length; i++) {
				if (newArray[i] == null){
					newArray[i] = new Double[newSizeColumn];
				}else {
					newArray[i] = (Double[])resizeArray(newArray[i],newSizeColumn); 
				}	
			}
			return newArray;
		}else{
			return new Double[newSizeRow][newSizeColumn];
		}
	}		
	
	static Integer[][] resizeArray(Integer[][] oldArray, int newSizeRow, int newSizeColumn) {
		if(oldArray!=null){
			Integer[][] newArray = (Integer[][])resizeGenericArray(oldArray,newSizeRow);
			for (int i=0; i<newArray.length; i++) {
				if (newArray[i] == null){
					newArray[i] = new Integer[newSizeColumn];
				}else {
					newArray[i] = (Integer[])resizeArray(newArray[i],newSizeColumn); 
				}	
			}
			return newArray;
		}else{
			return new Integer[newSizeRow][newSizeColumn];
		}
	}			
	
	private static Object resizeGenericArray (Object oldArray, int newSize) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		Class<?> elementType = oldArray.getClass().getComponentType();
		Object newArray = java.lang.reflect.Array.newInstance(
				elementType,newSize);
		int preserveLength = Math.min(oldSize,newSize);
		if (preserveLength > 0)
			System.arraycopy (oldArray,0,newArray,0,preserveLength);
		return newArray; 
	}


}
