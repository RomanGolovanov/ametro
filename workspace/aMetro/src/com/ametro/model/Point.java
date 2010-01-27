package com.ametro.model;

import java.io.Serializable;


public class Point extends android.graphics.Point implements Serializable {

	public Point(int x, int y) {
		this.x=x;
		this.y=y;
	}

	public Point(android.graphics.Point point) {
		this.x = point.x;
		this.y = point.y;
	}

	private static final long serialVersionUID = 4912765683919958967L;

}
