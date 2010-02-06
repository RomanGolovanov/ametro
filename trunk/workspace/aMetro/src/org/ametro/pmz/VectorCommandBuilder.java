/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com
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


public class VectorCommandBuilder {

    public static IVectorCommand createVectorCommand(FilePackage owner, VectorResource resource, String commandName, String arguments) {
        IVectorCommand cmd = null;
        /* */
        if (commandName.equalsIgnoreCase("Size")) {
            cmd = new VectorSize();
        } else if (commandName.equalsIgnoreCase("PenColor")) {
            cmd = new VectorPenColor();
        } else if (commandName.equalsIgnoreCase("Spline")) {
            cmd = new VectorSpline();
        } else if (commandName.equalsIgnoreCase("AngleTextOut")) {
            cmd = new VectorAngleTextOut();
        } else if (commandName.equalsIgnoreCase("BrushColor")) {
            cmd = new VectorBrushColor();
        } else if (commandName.equalsIgnoreCase("SpotRect")) {
            cmd = new VectorSpotRect();
        } else if (commandName.equalsIgnoreCase("Image")) {
            cmd = new VectorImage();
        } else if (commandName.equalsIgnoreCase("TextOut")) {
            cmd = new VectorTextOut();
        } else if (commandName.equalsIgnoreCase("Polygon")) {
            cmd = new VectorPolygon();
        } else if (commandName.equalsIgnoreCase("Line")) {
            cmd = new VectorLine();
        } else if (commandName.equalsIgnoreCase("SpotCircle")) {
            cmd = new VectorSpotCircle();
        } else if (commandName.equalsIgnoreCase("Arrow")) {
            cmd = new VectorArrow();
        } else {
            cmd = new VectorEmpty(); // TODO: return empty command if not recognized!
        }
        cmd.initialize(owner, resource, commandName, arguments);
        return cmd;
    }

}
