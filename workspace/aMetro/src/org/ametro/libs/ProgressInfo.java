package org.ametro.libs;

import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressInfo
{
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

	public static void ChangeProgress(ProgressInfo pi, ProgressBar bar, TextView title, TextView msg, TextView counter, String counterTemplate){
		if(bar == null || pi == null) return;
		bar.setMax(pi.Maximum);
		bar.setProgress(pi.Progress);
		if(pi.Message!=null && msg!=null){
			msg.setText(pi.Message);
		}
		if(pi.Title!=null && title!=null){
			title.setText(pi.Title);
		}
		if(counter!=null && !(pi.Progress==0 && pi.Maximum==0)){
			counter.setText( String.format(counterTemplate, pi.Progress, pi.Maximum) );
		}
	}
}

