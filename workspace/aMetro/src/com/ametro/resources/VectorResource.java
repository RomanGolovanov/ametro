package com.ametro.resources;

import java.util.ArrayList;

import com.ametro.resources.vector.CommandBuilder;
import com.ametro.resources.vector.ICommand;
import com.ametro.resources.vector.Size;

public class VectorResource implements IResource {

	private class VectorParser
	{
		private FilePackage owner;
		private VectorResource vectorResource;
		private ArrayList<ICommand> parsedCommands = new ArrayList<ICommand>();

		private ICommand[] getCommands(){
			return (ICommand[]) parsedCommands.toArray(new ICommand[parsedCommands.size()]);
		}

		public void parseLine(String line)
		{
			if(line.startsWith(";") || line.length() == 0) return;
			int firstSpaceIndex = line.indexOf(' ');
			if(firstSpaceIndex!= -1){
				String commandName = line.substring(0, firstSpaceIndex).trim();
				String arguments = line.substring(firstSpaceIndex).trim();
				ICommand cmd = CommandBuilder.createVectorCommand(owner, vectorResource,commandName,arguments);
				if(cmd instanceof Size){
					Size size = (Size)cmd;
					width = size.getWidth();
					height = size.getHeight();
				}
			}
		}

		public VectorParser(FilePackage owner, VectorResource vectorResource){

		}
	}

	public void beginInitialize(FilePackage owner) {
		parser = new VectorParser(owner,this);
	}

	public void doneInitialize() {
		this.commands = parser.getCommands();
		parser = null;
	}

	public void parseLine(String line) {
		parser.parseLine(line.trim());
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ICommand[] getCommands(){
		return commands;
	}

	private VectorParser parser;
	private ICommand[] commands;

	private int width;
	private int height;

}
