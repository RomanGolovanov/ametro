package com.ametro.resources.vector;

import com.ametro.resources.FilePackage;
import com.ametro.resources.VectorResource;

public interface ICommand {
	void initialize(FilePackage owner, VectorResource resource, String commandName, String arguments);
}
