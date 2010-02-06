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

package org.ametro.libs;

import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressInfo {
    public int Progress;
    public int Maximum;
    public String Message;
    public String Title;

    public ProgressInfo(int progress, int maximum, String message, String title) {
        super();
        this.Progress = progress;
        this.Maximum = maximum;
        this.Message = message;
        this.Title = title;
    }

    public static void ChangeProgress(ProgressInfo pi, ProgressBar bar, TextView title, TextView msg, TextView counter, String counterTemplate) {
        if (bar == null || pi == null) return;
        bar.setMax(pi.Maximum);
        bar.setProgress(pi.Progress);
        if (pi.Message != null && msg != null) {
            msg.setText(pi.Message);
        }
        if (pi.Title != null && title != null) {
            title.setText(pi.Title);
        }
        if (counter != null && !(pi.Progress == 0 && pi.Maximum == 0)) {
            counter.setText(String.format(counterTemplate, pi.Progress, pi.Maximum));
        }
    }
}

