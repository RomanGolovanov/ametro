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
package org.ametro.catalog.storage;

import java.io.BufferedOutputStream;

import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import static org.ametro.catalog.storage.CatalogDeserializer.TAG_CATALOG;
import static org.ametro.catalog.storage.CatalogDeserializer.TAG_MAP;
import static org.ametro.catalog.storage.CatalogDeserializer.TAG_LOCALE;
import static org.ametro.catalog.storage.CatalogDeserializer.TAG_COUNTRY;
import static org.ametro.catalog.storage.CatalogDeserializer.TAG_CITY;
import static org.ametro.catalog.storage.CatalogDeserializer.TAG_DESCRIPTION;
import static org.ametro.catalog.storage.CatalogDeserializer.TAG_CHANGE_LOG;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_URL;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_SYSTEM_NAME;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_LAST_MODIFIED;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_TRANSPORTS;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_VERSION;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_CODE;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_SIZE;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_MIN_VERSION;
import static org.ametro.catalog.storage.CatalogDeserializer.ATTR_CORRUPTED;;

public class CatalogSerializer {

	public static void serializeCatalog(Catalog catalog, BufferedOutputStream stream) {
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(stream, "UTF-8");
			serializer.startDocument("UTF-8", true);
			
			serializer.startTag("", TAG_CATALOG);
			serializer.attribute("", ATTR_LAST_MODIFIED, ""+catalog.getTimestamp());
			serializer.attribute("", ATTR_URL, ""+catalog.getBaseUrl());
			for (CatalogMap map : catalog.getMaps()) {
				serializer.startTag("", TAG_MAP);
				serializer.attribute("", ATTR_SYSTEM_NAME, map.getSystemName());
				serializer.attribute("", ATTR_URL, map.getUrl());
				serializer.attribute("", ATTR_LAST_MODIFIED, ""+map.getTimestamp());
				serializer.attribute("", ATTR_TRANSPORTS, ""+map.getTransports());
				serializer.attribute("", ATTR_VERSION, ""+map.getVersion());
				serializer.attribute("", ATTR_SIZE, ""+map.getSize());
				serializer.attribute("", ATTR_MIN_VERSION, ""+map.getMinVersion());
				serializer.attribute("", ATTR_CORRUPTED,  map.isCorruted() ? "true" : "false" );
				
				for(String localeCode : map.getLocales()){
					serializer.startTag("", TAG_LOCALE);
					serializer.attribute("", ATTR_CODE, localeCode);
					
					serializer.startTag("", TAG_COUNTRY);
					serializer.text(map.getCountry(localeCode));
					serializer.endTag("", TAG_COUNTRY);
					
					serializer.startTag("", TAG_CITY);
					serializer.text(map.getCity(localeCode));
					serializer.endTag("", TAG_CITY);
					
					serializer.startTag("", TAG_DESCRIPTION);
					serializer.text(map.getDescription(localeCode));
					serializer.endTag("", TAG_DESCRIPTION);

					serializer.startTag("", TAG_CHANGE_LOG);
					serializer.text(map.getChangeLog(localeCode));
					serializer.endTag("", TAG_CHANGE_LOG);

					serializer.endTag("", TAG_LOCALE);
				}
				serializer.endTag("", TAG_MAP);
			}
			serializer.endTag("", TAG_CATALOG);
			serializer.endDocument();
			stream.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
