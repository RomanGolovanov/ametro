/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.ametro.model.storage;

import java.io.IOException;
import java.util.Locale;

import org.ametro.model.Model;
import org.ametro.util.StringUtil;

public class ModelBuilder {

	//private static final int BUFFER_SIZE = 8196;
	
	private static IModelStorage getStorage(String fileName){
		if(StringUtil.isEmpty(fileName)) return null;
		if(fileName.toLowerCase().endsWith(".pmz")) return new PmzStorage();
		if(fileName.toLowerCase().endsWith(".ametro")) return new CsvStorage();
		return null;
	}

	public static Model loadModel(String fileName) throws IOException{
		return loadModel(fileName,Locale.getDefault());
	}
	
	public static Model loadModel(String fileName, Locale locale) throws IOException{
		IModelStorage storage = getStorage(fileName);
		if(storage!=null){
			Model model = storage.loadModel(fileName, locale);
			return model;
		}
		
		return null;
	}

	public static Model loadModelDescription(String fileName) throws IOException{
		return loadModelDescription(fileName, Locale.getDefault());
	}
	
	public static Model loadModelDescription(String fileName, Locale locale) throws IOException{
		IModelStorage storage = getStorage(fileName);
		if(storage!=null){
			return storage.loadModelDescription(fileName, locale);
		}
		return null;
	}
	
	public static void saveModel(String fileName, Model model) throws IOException{
		IModelStorage storage = getStorage(fileName);
		if(storage!=null){
			storage.saveModel(fileName, model);
		}
	}
	
//	public static void loadModelLocale(String fileName, Model model, Locale locale) throws IOException{
//		IModelStorage storage = getStorage(fileName);
//		if(storage!=null){
//			storage.loadModelLocale(fileName, model, locale);
//		}	
//	}

	
}
