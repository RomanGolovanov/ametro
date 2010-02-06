package org.ametro.libs;

import android.graphics.PointF;

class Solve2x2 {
	float __determinant = 0;

	public PointF solve( float _a11, float _a12, float _a21, float _a22, float _b1, float _b2, float zeroTolerance, boolean _resolve )
	{
		if( !_resolve )
		{
			__determinant = _a11*_a22 - _a12*_a21;
		}

		// exercise - dispatch an event if the determinant is near zero
		if( __determinant > zeroTolerance )
		{
			float x = (_a22*_b1 - _a12*_b2)/__determinant;
			float y = (_a11*_b2 - _a21*_b1)/__determinant;
			return new PointF(x,y);          
		}
		return null;
	}

}
