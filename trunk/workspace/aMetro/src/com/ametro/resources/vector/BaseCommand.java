package com.ametro.resources.vector;

import com.ametro.resources.FilePackage;
import com.ametro.resources.VectorResource;

public abstract class BaseCommand implements ICommand {

	@Override
	public void initialize(FilePackage owner, VectorResource resource, String commandName, String arguments) {
		// skip initialize! ^__^
	}
	
}
