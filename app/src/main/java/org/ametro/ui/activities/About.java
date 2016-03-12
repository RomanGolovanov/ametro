package org.ametro.ui.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ScrollView;
import android.widget.TextView;

import org.ametro.R;
import org.ametro.utils.FileUtils;

import java.io.IOException;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_view);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView aboutTextView = (TextView) findViewById(R.id.text);
        try {
            aboutTextView.setText(getAboutContent());
            aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected SpannableString getAboutContent() throws IOException {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(String.format("<p>%s %s</p>", getString(R.string.app_name), getVersionNumber())));
        builder.append(Html.fromHtml(FileUtils.readAllText(
                getResources().openRawResource(R.raw.about))));
        Linkify.addLinks(builder, Linkify.ALL);
        return new SpannableString(builder);
    }

    private String getVersionNumber(){
        PackageManager manager = getPackageManager();
        PackageInfo info;
        try {
            return manager.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "0.0.0.0";
        }

    }

}
