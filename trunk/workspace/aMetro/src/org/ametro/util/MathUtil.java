package org.ametro.util;

import android.graphics.PointF;
import android.util.FloatMath;

public class MathUtil {


	public static float distance(PointF p1, PointF p2) {
		float x = p1.x - p2.x;
		float y = p1.y - p2.y;
		return FloatMath.sqrt(x * x + y * y);
	}	
}
