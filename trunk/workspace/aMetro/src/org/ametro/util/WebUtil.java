package org.ametro.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUtil {

	public static InputStream executeHttpGetRequest(URL url) throws IOException{
		HttpURLConnection  conn = (HttpURLConnection)url.openConnection();
		conn.setUseCaches(false);
		conn.setRequestMethod("GET");
		return conn.getInputStream();
	}
	
	public static void downloadFile(String downloadUrl, String fileName) throws IOException{
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try{
			in = new BufferedInputStream( executeHttpGetRequest(new URL(downloadUrl)) );
			out = new BufferedOutputStream( new FileOutputStream(fileName) );
			FileUtil.writeToStream(in,out,false);
		}finally{
			if(in!=null){
				try { in.close(); } catch (Exception e) { }
			}
			if(out!=null){
				try { out.close(); } catch (Exception e) { }
			}
		}
		
	}
	
	
	
}
