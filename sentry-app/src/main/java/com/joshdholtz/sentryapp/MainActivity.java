package com.joshdholtz.sentryapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.joshdholtz.sentry.Sentry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String yourDSN = "your-dsn";
        Sentry.init(this, yourDSN);
        Sentry.debug = true;


        Sentry.setMaxBreadcrumbs(8);
        for (int i=0; i<15; i++) {
            Sentry.addBreadcrumb("Limit Test", Integer.toString(i));
        }
        Sentry.captureMessage("8 breadcrumbs test.");

        Sentry.addNavigationBreadcrumb("activity.main", "here", "there");
        Sentry.addHttpBreadcrumb("http://example.com", "GET", 202);

        Sentry.captureEvent(new Sentry.SentryEventBuilder()
            .setMessage("This event has a message and a stacktrace.")
            .setStackTrace(Thread.currentThread().getStackTrace())
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void crash() {
        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    String s = null;
                    s.length();
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void onClickBreak(View view) {
        Sentry.addBreadcrumb("button.click", "break button");
        crash();

    }

    public void onClickCapture(View view) {
        Sentry.addBreadcrumb("button.click", "capture button");
        try {
            crash();
        } catch (Exception e) {
            Map<String, String> tags = new HashMap<>();
            tags.put("color", "yellow");
            tags.put("shape", "square");
            Sentry.captureEvent(new Sentry.SentryEventBuilder()
                .setException(e)
                .setMessage("Exception caught in click handler")
                .setServerName("https://badssl.com/")
                .setCulprit("https://untrusted-root.badssl.com/")
                .setLevel(Sentry.SentryEventLevel.WARNING)
                .setLogger("A logger")
                .setRelease("f035a895a5167ebd20a597d47761e033995e6689")
                .setTags(tags));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
