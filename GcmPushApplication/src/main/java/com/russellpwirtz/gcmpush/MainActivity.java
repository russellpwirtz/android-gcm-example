package com.russellpwirtz.gcmpush;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.russellpwirtz.library.LibraryActivity;
import com.russellpwirtz.library.LibraryUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main activity for our sample application.
 */
public class MainActivity extends Activity {

    private static Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private static String sourceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTextUsingLibraryMethod((TextView) findViewById(R.id.my_text_view));

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                InstanceID instanceID = InstanceID.getInstance(MainActivity.this);
                String sourceId = strings[0];

                String token = null;
                try {
                    token = instanceID.getToken(strings[0], GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                } catch (IOException e) {
                    LOGGER.error("Couldn't get token using sourceId: " + sourceId);
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Got GCM token: {} using sourceId: {}", token, sourceId);
                }

                return token;
            }
        }.execute(sourceId == null ? getString(R.string.gcm_defaultSenderId) : sourceId);
    }

    private void setTextUsingLibraryMethod(TextView textView) {
        final String librarySays = LibraryUtil.sayHello();
        textView.setText(librarySays);
    }

    /**
     * Starts an activity that is defined in the library module.
     * <p>
     * This method is called from our layout.
     */
    public void dispatchToActivity(View v) {
        startActivity(new Intent(this, LibraryActivity.class));
    }
}
