package com.ametro.pmz;


public class VectorCommandBuilder {

	public static IVectorCommand createVectorCommand(FilePackage owner, VectorResource resource, String commandName, String arguments) {
		IVectorCommand cmd = null;
		/* */ if(commandName.equalsIgnoreCase("Size")){
			cmd = new VectorSize();
		}else if(commandName.equalsIgnoreCase("PenColor")){
			cmd =  new VectorPenColor();
		}else if(commandName.equalsIgnoreCase("Spline")){
			cmd =  new VectorSpline();
		}else if(commandName.equalsIgnoreCase("AngleTextOut")){
			cmd =  new VectorAngleTextOut();
		}else if(commandName.equalsIgnoreCase("BrushColor")){
			cmd =  new VectorBrushColor();
		}else if(commandName.equalsIgnoreCase("SpotRect")){
			cmd =  new VectorSpotRect();
		}else if(commandName.equalsIgnoreCase("Image")){
			cmd =  new VectorImage();
		}else if(commandName.equalsIgnoreCase("TextOut")){
			cmd =  new VectorTextOut();
		}else if(commandName.equalsIgnoreCase("Polygon")){
			cmd =  new VectorPolygon();
		}else if(commandName.equalsIgnoreCase("Line")){
			cmd =  new VectorLine();
		}else if(commandName.equalsIgnoreCase("SpotCircle")){
			cmd =  new VectorSpotCircle();
		}else  if(commandName.equalsIgnoreCase("Arrow")){
			cmd =  new VectorArrow();
		}else{
			cmd = new VectorEmpty(); // TODO: return empty command if not recognized!
		}
		cmd.initialize(owner, resource, commandName, arguments);
		return cmd;
	}

}
