package com.ametro.resources;

import java.util.*;

public class Helpers {


	public static ArrayList<String> parseStringArray(String value)
	{
		String[] parts = value.split(",");
		return new ArrayList<String>(Arrays.asList(parts));
	}

	public static ArrayList<Integer> parseIntegerArray(String value)
	{
		String[] parts = value.split(",");
		ArrayList<Integer> vals = new ArrayList<Integer>();
		for (int i = 0; i < parts.length; i++) {
			try{
				Integer val = Integer.parseInt(parts[i].trim());
				vals.add(val);
			}catch(Exception ex){
				vals.add(null);
			}
		}
		return vals;
	}		

	public static ArrayList<Double> parseDoubleArray(String value)
	{
		String[] parts = value.split(",");
		ArrayList<Double> vals = new ArrayList<Double>();
		for (int i = 0; i < parts.length; i++) {
			try{
				Double val = Double.parseDouble(parts[i].trim());
				vals.add(val);
			}catch(Exception ex){
				vals.add(null);
			}
		}
		return vals;
	}		
	
	public static ArrayList<Point> parsePointArray(String value)
	{
		String[] parts = value.split(",");
		ArrayList<Point> points = new ArrayList<Point>();
		for (int i = 0; i < parts.length/2; i++) {
			Point point = new Point();
			point.x = Integer.parseInt(parts[i*2].trim());
			point.y = Integer.parseInt(parts[i*2+1].trim());
			points.add(point);
		}
		return points;
	}

	public static ArrayList<Rectangle> parseRectangleArray(String value)
	{
		String[] parts = value.split(",");
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
		for (int i = 0; i < parts.length/4; i++) {
			Rectangle rectangle = new Rectangle();
			rectangle.x1 = Integer.parseInt(parts[i*4].trim());
			rectangle.y1 = Integer.parseInt(parts[i*4+1].trim());
			rectangle.x2 = /*rectangle.x1*/ + Integer.parseInt(parts[i*4+2].trim());
			rectangle.y2 = /*rectangle.y1*/ + Integer.parseInt(parts[i*4+3].trim());
			rectangles.add(rectangle);
		}
		return rectangles;
	}
	
	public static Rectangle parseRectangle(String value)
	{
		String[] parts = value.split(",");
		Rectangle rectangle = new Rectangle();
		rectangle.x1 = Integer.parseInt(parts[0].trim());
		rectangle.y1 = Integer.parseInt(parts[1].trim());
		rectangle.x2 = /*rectangle.x1*/ + Integer.parseInt(parts[2].trim());
		rectangle.y2 = /*rectangle.y1*/ + Integer.parseInt(parts[3].trim());
		return rectangle;
	}
		
}
