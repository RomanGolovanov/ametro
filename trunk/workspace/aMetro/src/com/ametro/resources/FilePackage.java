package com.ametro.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FilePackage {

	private ZipFile file;

	private Dictionary<String,MapResource> mapResources = new Hashtable<String, MapResource>();
	private Dictionary<String,TransportResource> transportResources = new Hashtable<String, TransportResource>();
	private Dictionary<String,TextResource> textResources = new Hashtable<String, TextResource>();
	private Dictionary<String,VectorResource> vectorResources = new Hashtable<String, VectorResource>();
	private Dictionary<String,ImageResource> imageResources = new Hashtable<String, ImageResource>();
	private Dictionary<String,GenericResource> genericResource = new Hashtable<String, GenericResource>();

	private GenericResource cityGenericResource;

	public MapResource getMapResource(String name)  throws IOException {
		MapResource resource = mapResources.get(name);
		if(resource==null){
			resource = new MapResource();
			loadResource(name,resource);
			mapResources.put(name,resource);
		}
		return resource;
	}

	public TransportResource getTransportResource(String name)  throws IOException {
		TransportResource resource = transportResources.get(name);
		if(resource==null){
			resource = new TransportResource();
			loadResource(name,resource);
			transportResources.put(name,resource);
		}
		return resource;
	}


	public TextResource getTextResource(String name)  throws IOException {
		TextResource resource = textResources.get(name);
		if(resource==null){
			resource = new TextResource();
			loadResource(name,resource);
			textResources.put(name,resource);
		}
		return resource;
	}
	public VectorResource getVectorResource(String name) throws IOException {
		VectorResource resource = vectorResources.get(name);
		if(resource==null){
			resource = new VectorResource();
			loadResource(name,resource);
			vectorResources.put(name,resource);
		}
		return resource;
	}

	public ImageResource getImageResource(String name) throws IOException {
		ImageResource resource = imageResources.get(name);
		if(resource==null){
			resource = new ImageResource();
			loadResource(name,resource);
			imageResources.put(name,resource);
		}
		return resource;
	}

	public GenericResource getGenericResource(String name) throws IOException {
		GenericResource resource = genericResource.get(name);
		if(resource==null){
			resource = new GenericResource();
			loadResource(name,resource);
			genericResource.put(name,resource);
		}
		return resource;
	}

	public GenericResource getCityGenericResource() throws IOException {
		
		if(cityGenericResource==null){
			GenericResource resource = new GenericResource();
			loadResource(findCityResourceEntry(),resource);
			cityGenericResource = resource;
		}
		return cityGenericResource;
	}

	public FilePackage(String fileName) throws IOException
	{
		file = new ZipFile(fileName);
	}

	public void close() throws IOException
	{
		file.close();
		file = null;
		mapResources = null;
		transportResources = null;
		textResources = null;
		vectorResources = null;
		imageResources = null;
		genericResource = null;
		cityGenericResource = null;
	}

	public String[] getMapNames(){
		return getNamesByExtension(".map");
	}

	public String[] getTransportNames(){
		return getNamesByExtension(".trp");
	}

	public String[] getImageNames(){
		return getNamesByExtension(".bmp");
	}

	public String[] getVectorNames(){
		return getNamesByExtension(".vec");
	}

	public String[] getTextNames(){
		return getNamesByExtension(".txt");
	}

	private String[] getNamesByExtension(String ext){
		Enumeration<? extends ZipEntry> entries = file.entries();
		ArrayList<String> names = new ArrayList<String>();
		while(entries.hasMoreElements()){
			String name = entries.nextElement().getName();
			if(name.endsWith(ext))
				names.add(name);
		}
		return (String[]) names.toArray(new String[names.size()]);
	}


	private ZipEntry findResourceEntry(String name){
		ZipEntry entry = file.getEntry(name);
		if(entry==null){
			//Enumerator<ZipEntry> 
			Enumeration<? extends ZipEntry> entries = file.entries();
			while(entries.hasMoreElements()){
				ZipEntry item = entries.nextElement();
				if(item.getName().equalsIgnoreCase(name)){
					entry = item;
					break;
				}

			}
		}
		return entry;
	}

	private ZipEntry findCityResourceEntry(){
		Enumeration<? extends ZipEntry> entries = file.entries();
		while(entries.hasMoreElements()){
			ZipEntry item = entries.nextElement();
			if(item.getName().endsWith(".cty")){
				return item;
			}

		}
		return null;
	}

	private void loadResource(String name, IResource observer) throws IOException
	{
		ZipEntry entry = findResourceEntry(name);
		loadResource(entry, observer);
	}

	private void loadResource(ZipEntry entry, IResource observer) throws IOException
	{
		InputStreamReader reader = new InputStreamReader( file.getInputStream(entry), Charset.forName("windows-1251") );
		BufferedReader input =  new BufferedReader(reader);
		try {
			observer.beginInitialize(this);
			String line = null;
			while (( line = input.readLine()) != null){
				observer.parseLine(line);
			}
			observer.doneInitialize();
		}
		finally {
			input.close();
		}
	}


}
