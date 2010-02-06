/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.pmz;

import java.util.ArrayList;


public class VectorResource implements IResource {

    private class VectorParser {
        private FilePackage owner;
        private VectorResource vectorResource;
        private ArrayList<IVectorCommand> parsedCommands = new ArrayList<IVectorCommand>();

        private IVectorCommand[] getCommands() {
            return parsedCommands.toArray(new IVectorCommand[parsedCommands.size()]);
        }

        public void parseLine(String line) {
            if (line.startsWith(";") || line.length() == 0) return;
            int firstSpaceIndex = line.indexOf(' ');
            if (firstSpaceIndex != -1) {
                String commandName = line.substring(0, firstSpaceIndex).trim();
                String arguments = line.substring(firstSpaceIndex).trim();
                IVectorCommand cmd = VectorCommandBuilder.createVectorCommand(owner, vectorResource, commandName, arguments);
                if (cmd instanceof VectorSize) {
                    VectorSize size = (VectorSize) cmd;
                    width = size.getWidth();
                    height = size.getHeight();
                }
            }
        }

        public VectorParser(FilePackage owner, VectorResource vectorResource) {

        }
    }

    public void beginInitialize(FilePackage owner) {
        parser = new VectorParser(owner, this);
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

    public IVectorCommand[] getCommands() {
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
