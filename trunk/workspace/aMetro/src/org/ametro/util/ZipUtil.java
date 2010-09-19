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
package org.ametro.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

	public static void unzip(File archive, File path) throws IOException{
		ZipInputStream zip = null;
		String fileName = null;
		try{
			if(!path.exists()){
				path.mkdirs();
			}
			zip = new ZipInputStream(new FileInputStream(archive));
			ZipEntry zipEntry; 
			while((zipEntry=zip.getNextEntry()) != null) { 
				fileName = zipEntry.getName();
				final File outputFile = new File(path, fileName); 
				FileUtil.writeToStream(new BufferedInputStream(zip), new FileOutputStream(outputFile), false);
				zip.closeEntry();
			}
			zip.close();
			zip = null;
		}finally{
			if(zip != null){
				try{ zip.close(); } catch(Exception e){}
			}
		}
	}
	
}
