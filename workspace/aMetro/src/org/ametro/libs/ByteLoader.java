/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com
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

package org.ametro.libs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ByteLoader {
    private static int kDEFAULT_CHUNK_SIZE = 8192;

    /**
     * Creates a stateless ByteLoader object that loads
     * bytes from stream, URL and ZipEntry data sources.
     */
    public ByteLoader() {
    }

    /**
     * Loads the specified zentry from the supplied ZipInputStream
     * into a byte array.
     *
     * @param zin Zip input stream for the archive file
     * @param zentry The actual zip entry to be read from the stream
     * @return Bytes for the zip entry or null if not found in stream
     * @throws java.io.IOException reports any IO problems that occur in the process
     */
    public byte[] loadBytesFromZipEntry(ZipInputStream zin, ZipEntry zentry)
            throws IOException {
        byte[] b = null;
        if (!zentry.isDirectory()) {
            ZipEntry ze;
            String zname = zentry.getName();
            while ((ze = zin.getNextEntry()) != null) {
                if (zname.equals(ze.getName())) {
                    int size = (int) zentry.getSize();
                    if (size == -1)
                        throw new IOException("zipentry has unspecified size -> " + zname);
                    b = loadBytesFromStreamForSize(zin, size);
                    break;
                }
            }
        }
        return b;
    }

    /**
     * A convenience method for loading a collection of ZipEntrys from
     * the supplied ZipInputStream.
     *
     * @param zin Input stream for the archive file to be searched
     * @param zipEntries Table of resource names mapped against their ZipEntry
     * @return The resource names mapped against their byte arrays
     * @throws java.io.IOException reports io errors that occurred during processing
     */
    public Hashtable<String, byte[]> loadBytesFromZipEntries(ZipInputStream zin, Hashtable<String, ZipEntry> zipEntries)
            throws IOException {
        Hashtable<String, byte[]> resultTable = new Hashtable<String, byte[]>();
        IOException iofailure = null;
        if (zipEntries.size() > 0) {
            try {
                ZipEntry ze;
                byte[] b;
                int size;

                while ((ze = zin.getNextEntry()) != null) {
                    if (!ze.isDirectory()) {
                        String zname = ze.getName();
                        if (zipEntries.containsKey(zname)) {
                            ze = zipEntries.get(zname);
                            if ((size = (int) ze.getSize()) != -1) {
                                try {
                                    if ((b = loadBytesFromStreamForSize(zin, size)) != null)
                                        resultTable.put(zname, b);
                                }
                                catch (IOException ioe) {
                                    iofailure = ioe;
                                }
                            }
                        }
                    }
                }
            }
            catch (IOException ioe) {
                iofailure = ioe;
            }
        }

        if (iofailure != null)
            throw iofailure;

        return resultTable;
    }

    /**
     * Load an array of bytes from the supplied url data source
     *
     * @param url The url to load the bytes from
     * @return Bytes from the url
     * @throws java.io.IOException reports io errors occurring during the method
     */
    public byte[] loadBytesFromURL(URL url) throws IOException {
        byte[] b = null;
        URLConnection con = url.openConnection();
        int size = con.getContentLength();
        InputStream in = null;

        try {
            if ((in = con.getInputStream()) != null)
                b = (size != -1) ? loadBytesFromStreamForSize(in, size) : loadBytesFromStream(in);
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch (IOException ignored) {
                }
        }
        return b;
    }

    /**
     * Loads the supplied number bytes from the given input stream
     *
     * @param in Stream to read the bytes from
     * @param size The number of bytes to read
     * @return bytes read from the stream
     * @throws java.io.IOException reports IO failures
     */
    public byte[] loadBytesFromStreamForSize(InputStream in, int size)
            throws IOException {
        int count, index = 0;
        byte[] b = new byte[size];

        // read in the bytes from input stream
        while ((count = in.read(b, index, size)) > 0) {
            size -= count;
            index += count;
        }
        return b;
    }

    /**
     * Loads bytes from the given input stream until the end of stream
     * is reached.  It reads in at kDEFAULT_CHUNK_SIZE chunks.
     *
     * @param in Stream to read the bytes from
     * @return Bytes read from the stream
     * @throws java.io.IOException reports IO failures
     */
    public byte[] loadBytesFromStream(InputStream in) throws IOException {
        return loadBytesFromStream(in, kDEFAULT_CHUNK_SIZE);
    }

    /**
     * Loads bytes from the given input stream until the end of stream
     * is reached.  Bytes are read in at the supplied <code>chunkSize</code>
     * rate.
     *
     * @param in Stream to read the bytes from
     * @return Bytes read from the stream
     * @throws java.io.IOException reports IO failures
     */
    public byte[] loadBytesFromStream(InputStream in, int chunkSize)
            throws IOException {
        if (chunkSize < 1)
            chunkSize = kDEFAULT_CHUNK_SIZE;

        int count;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] b = new byte[chunkSize];
        try {
            while ((count = in.read(b, 0, chunkSize)) > 0)
                bo.write(b, 0, count);
            byte[] thebytes = bo.toByteArray();
            return thebytes;
        }
        finally {
            bo.close();
            bo = null;
        }
    }
}