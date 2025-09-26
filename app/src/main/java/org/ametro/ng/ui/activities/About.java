package org.ametro.ng.ui.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.ametro.ng.R;
import org.ametro.ng.utils.FileUtils;

import java.io.IOException;

public class About extends AppCompatActivity {

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_view);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView aboutTextView = findViewById(R.id.text);
        try {
            aboutTextView.setText(getAboutContent());
            aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        scrollView = findViewById(R.id.scrollView);
        if (savedInstanceState != null) {
            final int y = savedInstanceState.getInt("scrollY", 0);
            scrollView.post(() -> scrollView.scrollTo(0, y));
        }

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (scrollView != null) {
            outState.putInt("scrollY", scrollView.getScrollY());
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
