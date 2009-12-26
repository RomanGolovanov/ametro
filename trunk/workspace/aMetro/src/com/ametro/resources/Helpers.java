package com.ametro.resources;

import java.util.*;

import android.graphics.Point;
import android.graphics.Rect;

public class Helpers {

	private static String[] split(String value){
//		int open, close;
//		while( (open = value.indexOf("(")) !=-1 ){
//			close = value.indexOf(")", open);
//			value = value.substring(0,open) + value.substring(close);
//		}
		value = value.replaceAll("/\\(.*\\)/", "");
		return value.split(",");
	}

	public static String[] parseStringArray(String value)
	{
		return split(value);
	}

	public static Integer[] parseIntegerArray(String value)
	{
		String[] parts = split(value);
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
		String[] parts = split(value);
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
		String[] parts = split(value);
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
		String[] parts = split(value);
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
		String[] parts = split(value);
		int x1 = Integer.parseInt(parts[0].trim());
		int y1 = Integer.parseInt(parts[1].trim());
		int x2 = x1 + Integer.parseInt(parts[2].trim());
		int y2 = y1 + Integer.parseInt(parts[3].trim());
		return new Rect(x1, y1, x2, y2);
	}

	public static Point parsePoint(String value) {
		String[] parts = split(value);
		int x = Integer.parseInt(parts[0].trim());
		int y = Integer.parseInt(parts[1].trim());
		return new Point(x,y);
	}
		
}
