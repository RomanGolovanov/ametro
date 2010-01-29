package com.ametro.pmz;

import java.util.ArrayList;


public class VectorResource implements IResource {

	private class VectorParser
	{
		private FilePackage owner;
		private VectorResource vectorResource;
		private ArrayList<IVectorCommand> parsedCommands = new ArrayList<IVectorCommand>();

		private IVectorCommand[] getCommands(){
			return (IVectorCommand[]) parsedCommands.toArray(new IVectorCommand[parsedCommands.size()]);
		}

		public void parseLine(String line)
		{
			if(line.startsWith(";") || line.length() == 0) return;
			int firstSpaceIndex = line.indexOf(' ');
			if(firstSpaceIndex!= -1){
				String commandName = line.substring(0, firstSpaceIndex).trim();
				String arguments = line.substring(firstSpaceIndex).trim();
				IVectorCommand cmd = VectorCommandBuilder.createVectorCommand(owner, vectorResource,commandName,arguments);
				if(cmd instanceof VectorSize){
					VectorSize size = (VectorSize)cmd;
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

	public IVectorCommand[] getCommands(){
		return commands;
	}

	private VectorParser parser;
	private IVectorCommand[] commands;

	private int width;
	private int height;
	private long mCrc;

	public long getCrc() {
		return mCrc;
	}

	public void setCrc(long crc) {
		mCrc = crc;
	}	

}
