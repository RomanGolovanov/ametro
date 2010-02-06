package org.ametro.render;

import org.ametro.libs.ExtendedPath;
import org.ametro.model.Line;
import org.ametro.model.Model;
import org.ametro.model.Segment;
import org.ametro.model.Station;

import android.graphics.Canvas;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;


public class RenderSegment extends RenderElement {

	public Paint Paint;
	public ExtendedPath Path;
	
	public RenderSegment(Model model, Segment segment) {
		super();
		final Station from = segment.getFrom();
		final Station to = segment.getTo();
		final Line line = from.getLine();
		
		final Paint paint = new Paint();
		final ExtendedPath path = new ExtendedPath();
		
		final Double delay = segment.getDelay();
		final boolean lineWorking = (delay != null && delay > 0);
		final int lineWidth = model.getLinesWidth(); 

		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		
		if(lineWorking){
			paint.setStrokeWidth( lineWidth );
			paint.setPathEffect(new CornerPathEffect(lineWidth*0.6f));
		}else{
			paint.setStrokeWidth( lineWidth*0.75f );
			paint.setPathEffect(new ComposePathEffect(
					new DashPathEffect(new float[]{ lineWidth*0.8f, lineWidth*0.2f }, 0),
					new CornerPathEffect(lineWidth*0.6f)
					));
		}
		paint.setColor(segment.getFrom().getLine().getColor());

		this.Paint = paint;
		drawSegmentPath(line, segment, from, to, path);
		this.Path = path;

		final int minx = Math.min(from.getPoint().x,to.getPoint().x);
		final int maxx = Math.max(from.getPoint().x,to.getPoint().x);
		final int miny = Math.min(from.getPoint().y,to.getPoint().y);
		final int maxy = Math.max(from.getPoint().y,to.getPoint().y);
		final Rect box = new Rect(minx,miny,maxx,maxy);
		final Point[] nodes = segment.getAdditionalNodes();
		if(nodes!=null){
			final int length = nodes.length;
			for (int i = 0; i < length; i++) {
				final Point node = nodes[i];
				box.union(node.x, node.y);
			}
		}
		setProperties(RenderProgram.TYPE_LINE, box );
	}

	private void drawSegmentPath(Line line, Segment segment, Station from, Station to, ExtendedPath path) {
		final Point pointFrom = from.getPoint();
		final Point pointTo = to.getPoint();
		final Point[] additionalPoints = segment.getAdditionalNodes();
		if(additionalPoints!=null){
			if( (segment.getFlags() & Segment.SPLINE) != 0 ){
				Point[] points = new Point[additionalPoints.length+2];
				points[0] = pointFrom;
				points[points.length-1] = pointTo;
				for (int i = 0; i < additionalPoints.length; i++) {
					Point point = additionalPoints[i];
					points[i+1] = point;
				}
				path.drawSpline(points, 0, points.length);
			}else{
				path.moveTo(pointFrom.x, pointFrom.y);
				for (int i = 0; i < additionalPoints.length; i++) {
					path.lineTo(additionalPoints[i].x, additionalPoints[i].y);	
				}
				path.lineTo(pointTo.x, pointTo.y);
			}
		}else{
			path.moveTo(pointFrom.x, pointFrom.y);
			path.lineTo(pointTo.x, pointTo.y);
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		canvas.drawPath(Path, Paint);		
	}

}
