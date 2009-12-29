package com.ametro.libs;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class Algorithms {
	
	private final static float ZERO_TOLERANCE=0.00001f;
	
	private final static int LEFT = 1;
	private final static int RIGHT = 2;
	private final static int TOP = 4;
	private final static int BOTTOM = 8;
	
	
	public static float calculateDistance(Point p0, Point p1){
		int dx = p0.x - p1.x;
		int dy = p0.y - p1.y;
		return (float)Math.sqrt( dx*dx + dy*dy );
	}
	
	public static  float calculateAngle( float x0, float y0, float x, float y )
	{
		float angle = (float)(Math.atan( (y-y0)/(x-x0) ) / Math.PI * 180);
		float dx = x-x0;
		float dy = y-y0;
		if( angle > 0 ){
			if( dx < 0 && dy < 0 ){
				angle += 180;
			}
		}else if(angle < 0){
			if( dx < 0 && dy > 0 ){
				angle += 180;
			}else{
				angle += 360;
			}
		}else{
			if (dx<0)
			{
				angle = 180;
			}
		}
		return angle;
	}	

	public static PointF solve2x2( float _a11, float _a12, float _a21, float _a22, float _b1, float _b2,boolean _resolve )
	{
		float __determinant = 0;
		if( !_resolve )
		{
			__determinant = _a11*_a22 - _a12*_a21;
		}

		// exercise - dispatch an event if the determinant is near zero
		if( __determinant > ZERO_TOLERANCE )
		{
			float x = (_a22*_b1 - _a12*_b2)/__determinant;
			float y = (_a11*_b2 - _a21*_b1)/__determinant;
			return new PointF(x,y);          
		}
		return null;
	}

	public static PointF interpolateQuadBezier(Point p0, Point p1, Point p2){
		// compute t-value using chord-length parameterization
		float dx = p1.x - p0.x;
		float dy = p1.y - p0.y;
		float d1 = (float) Math.sqrt(dx*dx+dy*dy);
		float d = d1;

		dx = p2.x - p1.x;
		dy = p2.y - p1.y;
		d += (float) Math.sqrt(dx*dx+dy*dy);

		float t = d1/d;
		float t1 = 1.0f - t;
		float tSq = t * t;
		float denom = 2.0f * t * t1;

		PointF p = new PointF();
		p.x = (p1.x - t1*t1*p0.x - tSq*p2.x)/denom;
		p.y = (p1.y - t1*t1*p0.y - tSq*p2.y)/denom;

		return p;
	}

	public static QBezierControls interpolateCubeBezierSmooth(Point p0, Point p1, Point p2, Point p3, float smoothFactor){
		// Assume we need to calculate the control
		// points between (x1,y1) and (x2,y2).
		// Then x0,y0 - the previous vertex,
		//      x3,y3 - the next one.

		float x0 = p0.x;
		float y0 = p0.y;
		float x1 = p1.x;
		float y1 = p1.y;
		float x2 = p2.x;
		float y2 = p2.y;
		float x3 = p3.x;
		float y3 = p3.y;

		float xc1 = (x0 + x1) / 2.0f;
		float yc1 = (y0 + y1) / 2.0f;
		float xc2 = (x1 + x2) / 2.0f;
		float yc2 = (y1 + y2) / 2.0f;
		float xc3 = (x2 + x3) / 2.0f;
		float yc3 = (y2 + y3) / 2.0f;

		float len1 = (float) Math.sqrt((x1-x0) * (x1-x0) + (y1-y0) * (y1-y0));
		float len2 = (float) Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
		float len3 = (float) Math.sqrt((x3-x2) * (x3-x2) + (y3-y2) * (y3-y2));

		float k1 = len1 / (len1 + len2);
		float k2 = len2 / (len2 + len3);

		float xm1 = xc1 + (xc2 - xc1) * k1;
		float ym1 = yc1 + (yc2 - yc1) * k1;
		float xm2 = xc2 + (xc3 - xc2) * k2;
		float ym2 = yc2 + (yc3 - yc2) * k2;

		float ctrl1_x = xm1 + (xc2 - xm1) * smoothFactor + x1 - xm1;
		float ctrl1_y = ym1 + (yc2 - ym1) * smoothFactor + y1 - ym1;
		float ctrl2_x = xm2 + (xc2 - xm2) * smoothFactor + x2 - xm2;
		float ctrl2_y = ym2 + (yc2 - ym2) * smoothFactor + y2 - ym2;
		return new QBezierControls(ctrl1_x, ctrl1_y, ctrl2_x, ctrl2_y);
	}

	public static int vcode(Rect r, Point p) 
	{
		return (((p.x < r.left) ? LEFT : 0)  +  
		 ((p.x > r.right) ? RIGHT : 0) + 
		 ((p.y < r.top) ? TOP : 0)   + 
		 ((p.y > r.bottom) ? BOTTOM : 0)) ;
	}
		 
	public static boolean clipCohenSutherland(Rect r, Point a, Point b)
	{
		a = new Point(a);
		b = new Point(b);
		int code_a, code_b, code; 
		Point c; 
		code_a = vcode(r, a);
		code_b = vcode(r, b);
		while ( code_a!=0 || code_b!= 0 ) {
			if ( (code_a & code_b)!=0 )
				return false;
			if (code_a!=0) {
				code = code_a;
				c = a;
			} else {
				code = code_b;
				c = b;
			}
			if ( (code & LEFT)!=0 ) {
				c.y += (a.y - b.y) * (r.left - c.x) / (a.x - b.x);
				c.x = r.left;
			} else if ((code & RIGHT)!=0) {
				c.y += (a.y - b.y) * (r.right - c.x) / (a.x - b.x);
				c.x = r.right;
			}
			if ((code & TOP)!=0) {
				c.x += (a.x - b.x) * (r.top - c.y) / (a.y - b.y);
				c.y = r.top;
			} else if ((code & BOTTOM)!=0) {
				c.x += (a.x - b.x) * (r.bottom- c.y) / (a.y - b.y);
				c.y = r.bottom;
			}
			if (code == code_a)
				code_a = vcode(r,a);
			else
				code_b = vcode(r,b);
		}
		return true;
	}

	public static QBezierControls interpolateCubicBezierControl(Point p0, Point p1, Point p2, Point p3) {
		//TODO: change to correct interpolate method!
		return interpolateCubeBezierSmooth(p0, p1, p2, p3, 1.0f);
	}
	
}
