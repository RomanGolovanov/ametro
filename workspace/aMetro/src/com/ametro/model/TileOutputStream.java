package com.ametro.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.ametro.MapSettings;

public class TileOutputStream {

	private static final int BUFFER_SIZE = 32768;

	private ZipOutputStream content;

	public TileOutputStream(String fileName) throws FileNotFoundException{
		this(new File(fileName));
	}

	public TileOutputStream(File file) throws FileNotFoundException{
		content = 
			new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(file),BUFFER_SIZE));
	}

	public void write(ModelDescription description) throws IOException{
		ZipEntry entry = new ZipEntry(MapSettings.DESCRIPTION_ENTRY_NAME);
		description.setRenderVersion(MapSettings.getRenderVersion());
		content.putNextEntry(entry);
		ObjectOutputStream strm = new ObjectOutputStream(content);
		strm.writeObject(description);
		content.closeEntry();		
	}

	public void write(Tile tile) throws IOException{
		//Bitmap bmp = tile.getImage();
		String fileName = TileContainer.getTileEntityName(tile.getMapMapLevel(), tile.getRow(), tile.getColumn()); 
		ZipEntry entry = new ZipEntry(fileName);
		content.putNextEntry(entry);
		content.setLevel(-1);
		byte[] data = tile.getImage();
		content.write(data, 0, data.length);
		content.closeEntry();
	}

	public void close() throws IOException{
		content.close();
		content = null;
	}

}

