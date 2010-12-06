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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.ametro.app.ApplicationEx;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class WebUtil {
	
	private static class DownloadContext
	{
		public File Path;
		public long Total;
		public long Position;
		public boolean IsCanceled;
		public Notification Notification;
		public PendingIntent ContentIntent;
		public boolean IsUnpackFinished;
		public boolean IsFailed;
	}
	
	public static class DownloadCanceledException extends Exception {
		private static final long serialVersionUID = 1049597925954850843L;
	}
	
	public static  void downloadFileAsync(final Context appContext, final File path, final URI uri, final File temp) {
		
		final DownloadContext context = new DownloadContext();
		context.Path = path;
		context.IsCanceled = false;
		context.IsUnpackFinished = false;
		context.IsFailed = false;

		final NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE); 
		
		final Handler handler = new Handler();
		
		final Runnable updateProgress = new Runnable() {
			public void run() {
				if(context.Notification == null){
					context.Notification = new Notification(android.R.drawable.stat_sys_download,"Downloading icons", System.currentTimeMillis());
					context.Notification.flags = Notification.FLAG_NO_CLEAR;
					Intent notificationIntent = new Intent();
					context.ContentIntent = PendingIntent.getActivity(appContext, 0, notificationIntent, 0);					
				}
				if(context.IsFailed){
					notificationManager.cancelAll();
					context.Notification = new Notification(android.R.drawable.stat_sys_warning,"Icons download failed", System.currentTimeMillis());
					context.Notification.setLatestEventInfo(appContext, "aMetro", "Icons downloaded failed", context.ContentIntent);
					notificationManager.notify(2, context.Notification);
					
				}else if (context.IsUnpackFinished){
					notificationManager.cancelAll();
					context.Notification = new Notification(android.R.drawable.stat_sys_download_done,"Icons unpacked", System.currentTimeMillis());
					context.Notification.setLatestEventInfo(appContext, "aMetro", "Icons downloaded and unpacked.", context.ContentIntent);
					notificationManager.notify(3, context.Notification);
					
				}else if(context.Position==0 && context.Total == 0){
					context.Notification.setLatestEventInfo(appContext, "aMetro", "Download icons: connecting server", context.ContentIntent);
					notificationManager.notify(1, context.Notification);
				}else  if(context.Position < context.Total){
					context.Notification.setLatestEventInfo(appContext, "aMetro", "Download icons: " + context.Position + "/" + context.Total, context.ContentIntent);
					notificationManager.notify(1, context.Notification);
				}else{
					context.Notification.setLatestEventInfo(appContext, "aMetro", "Icons unpacking", context.ContentIntent);
					notificationManager.notify(1, context.Notification);
				}
			}
		};
		
		final Thread async = new Thread(){
			public void run() {
				WebUtil.downloadFile(context, uri, temp, false, new IDownloadListener(){

					public void onBegin(Object context, File file) {
						DownloadContext downloadContext = (DownloadContext)context;
						downloadContext.Total = 0;
						downloadContext.Position = 0;
						handler.removeCallbacks(updateProgress);
						handler.post(updateProgress);
					}
					
					public boolean onUpdate(Object context, long position, long total) {
						DownloadContext downloadContext = (DownloadContext)context;
						downloadContext.Total = total;
						downloadContext.Position = position;
						handler.removeCallbacks(updateProgress);
						handler.post(updateProgress);
						return !downloadContext.IsCanceled;
					}
					
					public void onDone(Object context, File file) throws Exception {
						DownloadContext downloadContext = (DownloadContext)context;
						File path = downloadContext.Path;
						ZipUtil.unzip(file, path);
						downloadContext.IsUnpackFinished=true;
						handler.removeCallbacks(updateProgress);
						handler.post(updateProgress);
					}

					public void onCanceled(Object context, File file) {
						DownloadContext downloadContext = (DownloadContext)context;
						downloadContext.IsCanceled=true;
						handler.removeCallbacks(updateProgress);
						handler.post(updateProgress);
					}

					public void onFailed(Object context, File file, Throwable reason) {
						DownloadContext downloadContext = (DownloadContext)context;
						downloadContext.IsFailed=true;
						handler.removeCallbacks(updateProgress);
						handler.post(updateProgress);
					}
					
				});
			};
		};
		async.start();
	}

	public static void downloadFileUnchecked(Object context, URI uri, File file, IDownloadListener listener) throws Exception{
		BufferedInputStream strm = null;
		if(listener!=null){
			listener.onBegin(context, file);
		}
		try{
			HttpClient client = ApplicationEx.getInstance().getHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(uri);
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() == 200){
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
			}else{
				String message =
					"Failed to download URL " + uri.toString() + 
					" due error " + status.getStatusCode() + " " + status.getReasonPhrase(); 
				throw new Exception(message);
			}
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
	}	
	
	public static void downloadFile(Object context, URI uri, File file, boolean reuse, IDownloadListener listener){
		BufferedInputStream strm = null;
		if(listener!=null){
			listener.onBegin(context, file);
		}
		try{
			HttpClient client = ApplicationEx.getInstance().getHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(uri);
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() == 200){
				HttpEntity entity = response.getEntity();
				long total = (int)entity.getContentLength();
				long position = 0;
				if(!(file.exists() && reuse && file.length() == total)){
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
				}
				if(listener!=null){
					listener.onDone(context, file);
				}	
			}else{
				String message =
					"Failed to download URL " + uri.toString() + 
					" due error " + status.getStatusCode() + " " + status.getReasonPhrase(); 
				throw new Exception(message);
			}
		}catch(DownloadCanceledException ex){
			if(listener!=null){
				listener.onCanceled(context, file);
			}		
		}catch(Exception ex){
			if(listener!=null){
				listener.onFailed(context, file, ex);
			}		
		}finally{
			if(strm!=null){
				try { strm.close(); }catch(IOException ex){}
			}
		}
	}
}
