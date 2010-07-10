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
