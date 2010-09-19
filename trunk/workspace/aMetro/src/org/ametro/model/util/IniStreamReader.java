/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.model.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IniStreamReader {

	private static final int BUFFER = 8196;
	
	private final BufferedReader mStream;
	
	private String mSection;
	private String mKey;
	private String mValue;
	
	private boolean mSectionChanged;
	
	public IniStreamReader(InputStreamReader stream) throws IOException{
		mStream = new BufferedReader(stream, BUFFER);
		mSection = null;
		mValue = null;
		mSectionChanged = false;
	}
	
	public String getSection(){
		return mSection;
	}

	public String getKey(){
		return mKey;
	}
	
	public String getValue(){
		return mValue;
	}
	
	public boolean isSectionChanged(){
		return mSectionChanged;
	}
	
	public boolean readNext() throws IOException{
		String line = mStream.readLine();
		boolean hasResult = false;
		mSectionChanged = false;
		while(line!=null){
			line = line.trim();
			if(line.length() != 0){
				if(line.startsWith("[") && line.endsWith("]") ){
					mSection = line.substring(1, line.length() - 1);
					mSectionChanged = true;
				}else if( line.indexOf('=')!=-1 && !(line.startsWith("#") || line.startsWith(";")) ) {
	                String[] parts = line.split("=");
                    mKey = parts[0].trim();
                    mValue = parts.length > 1 ? parts[1].trim() : "";
                    hasResult = true;
                    break;
				}
			}
			line = mStream.readLine();
		}
		return hasResult;
	}
	
}
