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

package org.ametro.libs;

public class DelaysString {

    private String mText;
    //private String[] mParts;
    private int mPos;
    private int mLen;

    public DelaysString(String text) {
        //text = text.replaceAll("\\(","");
        //text = text.replaceAll("\\)","");
        //mParts = text.split(",");
        mText = text;
        mLen = text != null ? mText.length() : 0;
        mPos = 0;
    }

    public boolean beginBracket() {
        return mText != null && mPos < mLen && mText.charAt(mPos) == '(';
    }

    private String nextBlock() {
        if (mText == null) return null;
        int nextComma = mText.indexOf(",", beginBracket() ? mText.indexOf(")", mPos) : mPos);
        String block = nextComma != -1 ? mText.substring(mPos, nextComma) : mText.substring(mPos);
        mPos = nextComma != -1 ? nextComma + 1 : mLen;
        return block;
    }

    public Double next() {
        return Helpers.parseNullableDouble(nextBlock());
    }

    public Double[] nextBracket() {
        if (mText == null) return null;
        String block = nextBlock();
        return Helpers.parseDoubleArray(block.substring(1, block.length() - 1));
    }

}
