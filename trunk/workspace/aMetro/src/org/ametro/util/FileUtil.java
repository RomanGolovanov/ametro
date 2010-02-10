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

package org.ametro.util;

import android.util.Log;

import java.io.File;

import static org.ametro.Constants.LOG_TAG_MAIN;

/**
 * @author Vlad Vinichenko (akerigan@gmail.com)
 *         Date: 06.02.2010
 *         Time: 19:59:03
 */
public class FileUtil {

    public static void delete(File file) {
        if (file != null && file.exists() && !file.delete() && Log.isLoggable(LOG_TAG_MAIN, Log.WARN)) {
            Log.w(LOG_TAG_MAIN, "Can't delete file: '" + file.toString() + "'");
        }
    }

    public static void move(File src, File dest) {
        if (src != null && src.exists() && !src.renameTo(dest) && Log.isLoggable(LOG_TAG_MAIN, Log.WARN)) {
            Log.w(LOG_TAG_MAIN, "Can't move file '" + src.toString() + "' to '" + dest.toString() + "'");
        }
    }

}
