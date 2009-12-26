package com.ametro.resources.vector;

import com.ametro.resources.FilePackage;
import com.ametro.resources.VectorResource;

public class Size extends BaseCommand {

	private int width;
	private int height;

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	@Override
	public void initialize(FilePackage owner, VectorResource resource, String commandName, String arguments) {
		super.initialize(owner, resource, commandName, arguments);
		String[] parts = arguments.split("x");
		width = Integer.parseInt(parts[0]);
		height = Integer.parseInt(parts[1]);
	}
	
	
}
