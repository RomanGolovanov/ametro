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


public class Tile {

    public static final float SCALE = 1.4f;
    public static final int MIP_MAP_LEVELS = 5;

    public static final int WIDTH = 100;
    public static final int HEIGHT = 100;

    private int mMipMapLevel;
    private int mRow;
    private int mColumn;
    private byte[] mImage;

    public int getRow() {
        return mRow;
    }

    public int getColumn() {
        return mColumn;
    }

    public int getMapMapLevel() {
        return mMipMapLevel;
    }

    public byte[] getImage() {
        return mImage;
    }

    public Tile(int row, int column, int mipMapLevel, byte[] image) {
        super();
        this.mRow = row;
        this.mColumn = column;
        this.mMipMapLevel = mipMapLevel;
        this.mImage = image;
    }

    public static float getScale(int level) {
        if (level == 0) return 1.0f;
        return (float) Math.exp(level * Math.log(Tile.SCALE));//(level+1) * Tile.SCALE;
    }

    public static int getDimension(int base, int level) {
        if (level == 0) return base;
        return (int) ((float) base / getScale(level));
    }

    public static int getTileCount(int dimensionSize) {
        return dimensionSize / WIDTH + ((dimensionSize % WIDTH) != 0 ? 1 : 0);
    }

    public static int getTileCount(int dimensionSize, int level) {
        if (level == 0) return getTileCount(dimensionSize);
        int newSize = getDimension(dimensionSize, level);
        return newSize / WIDTH + ((newSize % WIDTH) != 0 ? 1 : 0);
    }

}
