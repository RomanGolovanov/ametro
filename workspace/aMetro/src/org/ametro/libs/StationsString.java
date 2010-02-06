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

package org.ametro.libs;

public class StationsString {
    private String mText;
    private String mDelimeters;
    private int mPos;
    private int mLen;

    private boolean mBracketOpened;
    private String mLastDelimeter;
    private String mNextDelimeter;

    public boolean isBracketOpened() {
        return mBracketOpened;
    }

    public String getLastDelimeter() {
        return mLastDelimeter;
    }

    public String getNextDelimeter() {
        return mNextDelimeter;
    }

    public StationsString(String text) {
        mText = text;
        mDelimeters = ",()";
        mPos = 0;
        mLen = text.length();
        mBracketOpened = false;
        mLastDelimeter = null;
        skipToContent();
    }

    public boolean hasNext() {
        int saved = mPos;
        skipToContent();
        boolean result = mPos != mLen;
        mPos = saved;
        return result;
    }

    public String next() {
        skipToContent();
        if (mPos == mLen) {
            return "";
        }
        int pos = mPos;
        String symbol = null;
        boolean quotes = false;
        while (pos < mLen && (!mDelimeters.contains(symbol = mText.substring(pos, pos + 1)) || quotes)) {
            if ("\"".equals(symbol)) {
                quotes = !quotes;
            }
            pos++;
        }
        int end = symbol == null ? pos - 1 : pos;
        mNextDelimeter = symbol;
        String text = mText.substring(mPos, end);
        mPos = end;
        if (text.startsWith("\"") && text.endsWith("\""))
            text = text.substring(1, text.length() - 1);
        return text;
    }

    private void skipToContent() {
        String symbol = null;
        String symbolNext = (mPos < mLen) ? mText.substring(mPos, mPos + 1) : null;
        while (mPos < mLen && mDelimeters.contains(symbol = symbolNext)) {
            mLastDelimeter = symbol;
            if (symbol.equals("(")) {
                mBracketOpened = true;
                mPos++;
                return;

            } else if (symbol.equals(")")) {
                mBracketOpened = false;
            }
            mPos++;
            symbolNext = (mPos < mLen) ? mText.substring(mPos, mPos + 1) : null;
            if (",".equals(symbol) && !"(".equals(symbolNext)) return;
        }
    }


}


