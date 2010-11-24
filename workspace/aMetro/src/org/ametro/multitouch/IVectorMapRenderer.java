package org.ametro.multitouch;

import java.util.ArrayList;

import org.ametro.model.SchemeView;
import org.ametro.model.SegmentView;
import org.ametro.model.StationView;
import org.ametro.model.TransferView;
import org.ametro.render.RenderProgram;

import android.graphics.Canvas;
import android.graphics.Matrix;

public interface IVectorMapRenderer {

	public abstract void setScheme(SchemeView scheme, RenderProgram renderProgram);

	public abstract void setSchemeSelection(ArrayList<StationView> stations, ArrayList<SegmentView> segments, ArrayList<TransferView> transfers);

	public abstract void onAttachedToWindow();

	public abstract void onDetachedFromWindow();

	public abstract void setUpdatesEnabled(boolean enabled);

	public abstract boolean isUpdatesEnabled();

	public abstract void setAntiAliasEnabled(boolean enabled);

	//public abstract void setAntiAliasDisabledOnChanges(boolean disabled);

	public abstract void draw(Canvas canvas);

	/** set transformation matrix for content **/
	public abstract void setMatrix(Matrix newMatrix);

	/** get current transformation matrix**/
	public abstract Matrix getMatrix();

}