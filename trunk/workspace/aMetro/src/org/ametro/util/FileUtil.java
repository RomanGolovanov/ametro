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

import static org.ametro.app.Constants.LOG_TAG_MAIN;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.util.Log;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 06.02.2010
 *         Time: 19:59:03
 */ 
public class FileUtil {

	public static boolean delete(File file) {
		if (file != null && file.exists() && !file.delete() && Log.isLoggable(LOG_TAG_MAIN, Log.WARN)) {
			Log.w(LOG_TAG_MAIN, "Can't delete file: '" + file.toString() + "'");
			return false;
		}
		return true;
	}

	public static boolean delete(String file) {
		return delete(new File(file));
	}

	public static void move(File src, File dest) {
		if (src != null && src.exists() && !src.renameTo(dest) && Log.isLoggable(LOG_TAG_MAIN, Log.WARN)) {
			Log.w(LOG_TAG_MAIN, "Can't move file '" + src.toString() + "' to '" + dest.toString() + "'");
		}
	}

	public static long getLastModified(String filename) {
		return new File(filename).lastModified();
	}

	public static void createFile(File f) {
		try {
			f.createNewFile();
		} catch (IOException e) {
			// scoop exception
		}
	}
	
	public static void createDirectory(File path) {
		path.mkdirs();
	}

	public static String getFileName(String path){
		int lastSlashIndex = path.lastIndexOf(File.separatorChar);
		int lastBackslashIndex = path.lastIndexOf('\\');
		String fileName = path.substring( lastSlashIndex!=-1 ? (lastSlashIndex+1) : (lastBackslashIndex!=-1 ? (lastBackslashIndex+1) : 0) , path.lastIndexOf('.'));
		return fileName;
	}
	
	public static void writeToStream(InputStream in , OutputStream out, boolean closeOnExit) throws IOException 
	{
		byte[] bytes = new byte[2048];
		for (int c = in.read(bytes); c != -1; c = in.read(bytes)) {
			out.write(bytes,0, c);
		}
		if(closeOnExit){
			in.close();
			out.close();
		}
	}

	public static void touchDirectory(File file) {
		if(!file.exists()){
			file.mkdirs();
		}
	}		
	
	public static void touchFile(File file) {
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// scoop exception
			}
		}
	}

	public static boolean deleteAll(File file) {
		if(file!=null && file.exists()){
			if(file.isDirectory()){
				File[] children = file.listFiles();
				if(children!=null){
					for(File child : children){
						if(!deleteAll(child)){
							return false;
						}
					}
				}
			}
			return file.delete();
		}
		return true;
	}

	public static String writeToString(InputStream stream) throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream,"utf-8"));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
