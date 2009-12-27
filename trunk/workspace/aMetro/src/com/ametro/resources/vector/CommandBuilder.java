package com.ametro.resources.vector;

import java.security.InvalidParameterException;

import com.ametro.resources.FilePackage;
import com.ametro.resources.VectorResource;

public class CommandBuilder {

	public static ICommand createVectorCommand(FilePackage owner, VectorResource resource, String commandName, String arguments) {
		ICommand cmd = null;
		/* */ if(commandName.equalsIgnoreCase("Size")){
			cmd = new Size();
		}else if(commandName.equalsIgnoreCase("PenColor")){
			cmd =  new PenColor();
		}else if(commandName.equalsIgnoreCase("Spline")){
			cmd =  new Spline();
		}else if(commandName.equalsIgnoreCase("AngleTextOut")){
			cmd =  new AngleTextOut();
		}else if(commandName.equalsIgnoreCase("BrushColor")){
			cmd =  new BrushColor();
		}else if(commandName.equalsIgnoreCase("SpotRect")){
			cmd =  new SpotRect();
		}else if(commandName.equalsIgnoreCase("Image")){
			cmd =  new Image();
		}else if(commandName.equalsIgnoreCase("TextOut")){
			cmd =  new TextOut();
		}else if(commandName.equalsIgnoreCase("Polygon")){
			cmd =  new Polygon();
		}else if(commandName.equalsIgnoreCase("Line")){
			cmd =  new Line();
		}else if(commandName.equalsIgnoreCase("SpotCircle")){
			cmd =  new SpotCircle();
		}else{
		throw new InvalidParameterException("Command '" + commandName +"' not found");
		}
		cmd.initialize(owner, resource, commandName, arguments);
		return cmd;
	}

}
