package org.ametro.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.ametro.ApplicationEx;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class WebUtil {
	
	public static class DownloadCanceledException extends Exception {
		private static final long serialVersionUID = 1049597925954850843L;
	}
	
	public static void downloadFile(Object context, URI uri, File file, IOperationListener listener){
		BufferedInputStream strm = null;
		if(listener!=null){
			listener.onBegin(context);
		}
		try{
			HttpClient client = ApplicationEx.getInstance().getHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(uri);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();

			long total = (int)entity.getContentLength();
			long position = 0;
			
			if(file.exists()){
				file.delete();
			}

			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			try{
				in = new BufferedInputStream( entity.getContent() );
				out = new BufferedOutputStream( new FileOutputStream(file) );
				byte[] bytes = new byte[2048];
				for (int c = in.read(bytes); c != -1; c = in.read(bytes)) {
					out.write(bytes,0, c);
					position += c;
					if(listener!=null){
						if(!listener.onUpdate(context, position, total)){
							throw new DownloadCanceledException();
						}
					}
				}
				
			}finally{
				if(in!=null){
					try { in.close(); } catch (Exception e) { }
				}
				if(out!=null){
					try { out.close(); } catch (Exception e) { }
				}
			}	
			if(listener!=null){
				listener.onDone(context, file);
			}	
		}catch(DownloadCanceledException ex){
			if(listener!=null){
				listener.onCanceled(context);
			}		
		}catch(Exception ex){
			if(listener!=null){
				listener.onFailed(context, ex);
			}		
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
	}
}
