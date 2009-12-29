package com.ametro.libs;

import java.util.ArrayList;

import android.graphics.Point;
import android.graphics.Rect;

public class Helpers {

	private static String[] splitCommaSeparaterString(String value){
		value = value.replaceAll("/\\(.*\\)/", "");
		return value.split(",");
	}

	public static String[] parseStringArray(String value)
	{
		return splitCommaSeparaterString(value);
	}

	public static Integer[] parseIntegerArray(String value)
	{
		String[] parts = splitCommaSeparaterString(value);
		ArrayList<Integer> vals = new ArrayList<Integer>();
		for (int i = 0; i < parts.length; i++) {
			try{
				Integer val = Integer.parseInt(parts[i].trim());
				vals.add(val);
			}catch(Exception ex){
				vals.add(null);
			}
		}
		return (Integer[]) vals.toArray(new Integer[vals.size()]);
	}		

	public static Double[] parseDoubleArray(String value)
	{
		String[] parts = splitCommaSeparaterString(value);
		ArrayList<Double> vals = new ArrayList<Double>();
		for (int i = 0; i < parts.length; i++) {
			try{
				Double val = Double.parseDouble(parts[i].trim());
				vals.add(val);
			}catch(Exception ex){
				vals.add(null);
			}
		}
		return (Double[]) vals.toArray(new Double[vals.size()]);
	}		
	
	public static Point[] parsePointArray(String value)
	{
		String[] parts = splitCommaSeparaterString(value);
		ArrayList<Point> points = new ArrayList<Point>();
		for (int i = 0; i < parts.length/2; i++) {
			Point point = new Point();
			point.x = Integer.parseInt(parts[i*2].trim());
			point.y = Integer.parseInt(parts[i*2+1].trim());
			points.add(point);
		}
		return (Point[]) points.toArray(new Point[points.size()]);
	}

	public static Rect[] parseRectangleArray(String value)
	{
		String[] parts = splitCommaSeparaterString(value);
		ArrayList<Rect> rectangles = new ArrayList<Rect>();
		for (int i = 0; i < parts.length/4; i++) {
			int x1 = Integer.parseInt(parts[i*4].trim());
			int y1 = Integer.parseInt(parts[i*4+1].trim());
			int x2 = x1 + Integer.parseInt(parts[i*4+2].trim());
			int y2 = y1 + Integer.parseInt(parts[i*4+3].trim());
			rectangles.add(new Rect(x1, y1, x2, y2));
		}
		return (Rect[]) rectangles.toArray(new Rect[rectangles.size()]);
	}
	
	public static Rect parseRectangle(String value)
	{
		String[] parts = splitCommaSeparaterString(value);
		int x1 = Integer.parseInt(parts[0].trim());
		int y1 = Integer.parseInt(parts[1].trim());
		int x2 = x1 + Integer.parseInt(parts[2].trim());
		int y2 = y1 + Integer.parseInt(parts[3].trim());
		return new Rect(x1, y1, x2, y2);
	}

	public static Point parsePoint(String value) {
		String[] parts = splitCommaSeparaterString(value);
		int x = Integer.parseInt(parts[0].trim());
		int y = Integer.parseInt(parts[1].trim());
		return new Point(x,y);
	}
	
	public static String convertCommas(String str)
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
	

	public static boolean[] resizeArray(boolean[] oldArray, int newSize) {
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

	public static int[] resizeArray(int[] oldArray, int newSize) {
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

	public static Integer[] resizeArray(Integer[] oldArray, int newSize) {
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
	
	public static String[] resizeArray(String[] oldArray, int newSize) {
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

	public static Rect[] resizeArray(Rect[] oldArray, int newSize) {
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

	public static Point[] resizeArray(Point[] oldArray, int newSize) {
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


	public static Double[] resizeArray(Double[] oldArray, int newSize) {
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

	

	public static boolean[][] resizeArray(boolean[][] oldArray, int newSizeRow, int newSizeColumn) {
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
	
	public static int[][] resizeArray(int[][] oldArray, int newSizeRow, int newSizeColumn) {
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
	
	public static Point[][] resizeArray(Point[][] oldArray, int newSizeRow, int newSizeColumn) {
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
	
	public static Double[][] resizeArray(Double[][] oldArray, int newSizeRow, int newSizeColumn) {
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
	
	public static Integer[][] resizeArray(Integer[][] oldArray, int newSizeRow, int newSizeColumn) {
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
