package com.russellpwirtz.gcmpush;

import com.google.android.gms.iid.InstanceIDListenerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushInstanceIDListenerService extends InstanceIDListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushInstanceIDListenerService.class);

    // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
    public void onTokenRefresh() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Received token refresh!");
        }

//          TODO: handle token refresh
//        Intent intent = new Intent(this, RegistrationIntentService.class);
//        startService(intent);
    }
}
