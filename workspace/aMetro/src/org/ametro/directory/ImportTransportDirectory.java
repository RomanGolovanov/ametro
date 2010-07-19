package org.ametro.directory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ametro.Constants;
import org.ametro.model.TransportType;
import org.ametro.util.csv.CsvReader;

import android.content.Context;
import android.util.Log;

public class ImportTransportDirectory {

	public static class TransportMapEntity
	{
		private String mFileName;
		private String mTransportFileName;
		private int mTransportType;

		public String getFileName() {
			return mFileName;
		}
		
		public String getTransportFileName() {
			return mTransportFileName;
		}
		
		public int getTransportType(){
			return mTransportType;
		}
		

		public TransportMapEntity(String fileName, String transportFileName, int transportType) {
			super();
			this.mFileName = fileName;
			this.mTransportFileName = transportFileName;
			this.mTransportType = transportType;
		}

	}
	
	public ImportTransportDirectory(Context context) {
		mIndex = new HashMap<String, TransportMapEntity>();
		try {
			InputStream strm = context.getAssets().open("transports.dict");
			CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(strm, "utf-8")),',');
			if(reader.next()){
				while(reader.next()){
					String fileName = reader.readString();
					String transportFileName = reader.readString();
					int transports = TransportType.getTransportTypeId( reader.readString() );
					TransportMapEntity entity = new TransportMapEntity(fileName, transportFileName, transports);
					mIndex.put(getEntityId(entity), entity);
				}
			}
		} catch (Throwable e) {
			if(Log.isLoggable(Constants.LOG_TAG_MAIN, Log.ERROR)){
				Log.e(Constants.LOG_TAG_MAIN,"Failed import transport directory creation: " + e.toString());
			}
		}
	}
	
	public TransportMapEntity get(String fileName, String tranposrtFileName){
		return mIndex.get(fileName.toLowerCase() + ":" + tranposrtFileName.toLowerCase());
	}
	
	private static String getEntityId(TransportMapEntity entity){
		return entity.getFileName().toLowerCase()+ ".pmz" + ":" + entity.getTransportFileName().toLowerCase() + ".trp";
	}
	
	private final HashMap<String, TransportMapEntity> mIndex;
}
