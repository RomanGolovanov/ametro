package org.ametro.ui.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


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
        if (actionBar != null) {
            actionBar.setDefaultDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView aboutTextView = findViewById(R.id.text);
        try {
            aboutTextView.setText(getAboutContent());
            aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected SpannableString getAboutContent() throws IOException {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(
                String.format("<p>%s %s</p>", getString(R.string.app_name), getVersionNumber()),
                Html.FROM_HTML_MODE_LEGACY   // <-- safer on newer SDKs
        ));
        builder.append(Html.fromHtml(
                FileUtils.readAllText(getResources().openRawResource(R.raw.about)),
                Html.FROM_HTML_MODE_LEGACY
        ));
        Linkify.addLinks(builder, Linkify.ALL);
        return new SpannableString(builder);
    }

    private String getVersionNumber() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "0.0.0.0";
        }
    }
}
