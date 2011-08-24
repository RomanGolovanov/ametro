package org.ametro.render;

import org.ametro.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;

public class MapMarkerRenderer {

	public static final int MARKER_TYPE_START = 0;
	public static final int MARKER_TYPE_END = 1;
	
	public MapMarkerRenderer(Context context){
		
		mMarkers = new Bitmap[2];
		mPositions = new Point[2];
		
		mMarkers[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.map_marker_start);
		mMarkers[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.map_marker_end);

		mPositions[0] = new Point(-3, -21);
		mPositions[1] = new Point(-3,-21);
	}
	
	public void drawStartMarker(Canvas canvas, Matrix positionAndScale, int markerX, int markerY, int markerType){
		float[] values = new float[9];
		positionAndScale.getValues(values);
		float scale = values[Matrix.MSCALE_X];
		float x = -values[Matrix.MTRANS_X];
		float y = -values[Matrix.MTRANS_Y];
		Point delta = mPositions[markerType];
		canvas.drawBitmap(mMarkers[markerType] , 
				markerX * scale - x + delta.x,
				markerY * scale - y + delta.y,
				null);
	}

	private Bitmap[] mMarkers;
	private Point[] mPositions;
}
