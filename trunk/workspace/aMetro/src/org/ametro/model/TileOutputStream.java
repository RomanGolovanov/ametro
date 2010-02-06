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

package org.ametro.model;

import org.ametro.MapSettings;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class TileOutputStream {

    private static final int BUFFER_SIZE = 32768;

    private ZipOutputStream content;

    public TileOutputStream(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    public TileOutputStream(File file) throws FileNotFoundException {
        content =
                new ZipOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(file), BUFFER_SIZE));
    }

    public void write(ModelDescription description) throws IOException {
        ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
        description.setRenderVersion(MapSettings.getRenderVersion());
        content.putNextEntry(entry);
        ObjectOutputStream strm = new ObjectOutputStream(content);
        strm.writeObject(description);
        content.closeEntry();
    }

    public void write(Tile tile) throws IOException {
        //Bitmap bmp = tile.getImage();
        String fileName = TileContainer.getTileEntityName(tile.getMapMapLevel(), tile.getRow(), tile.getColumn());
        ZipEntry entry = new ZipEntry(fileName);
        content.putNextEntry(entry);
        content.setLevel(-1);
        byte[] data = tile.getImage();
        content.write(data, 0, data.length);
        content.closeEntry();
    }

    public void close() throws IOException {
        content.close();
        content = null;
    }

}

