package com.ametro.resources.vector;

import java.security.InvalidParameterException;

import com.ametro.resources.FilePackage;
import com.ametro.resources.VectorResource;

public class CommandBuilder {

	public static ICommand createVectorCommand(FilePackage owner, VectorResource resource, String commandName, String arguments) {
		ICommand cmd = null;
		/* */ if(commandName.equals("Size")){
			cmd = new Size();
		}else if(commandName.equals("PenColor")){
			cmd =  new PenColor();
		}else if(commandName.equals("Spline")){
			cmd =  new Spline();
		}else if(commandName.equals("AngleTextOut")){
			cmd =  new AngleTextOut();
		}else if(commandName.equals("BrushColor")){
			cmd =  new BrushColor();
		}else if(commandName.equals("SpotRect")){
			cmd =  new SpotRect();
		}else if(commandName.equals("Image")){
			cmd =  new Image();
		}else if(commandName.equals("TextOut")){
			cmd =  new TextOut();
		}else if(commandName.equals("Polygon")){
			cmd =  new Polygon();
		}else if(commandName.equals("Line")){
			cmd =  new Line();
		}else if(commandName.equals("SpotCircle")){
			cmd =  new SpotCircle();
		}else{
		throw new InvalidParameterException("Command '" + commandName +"' not found");
		}
		cmd.initialize(owner, resource, commandName, arguments);
		return cmd;
	}

}
