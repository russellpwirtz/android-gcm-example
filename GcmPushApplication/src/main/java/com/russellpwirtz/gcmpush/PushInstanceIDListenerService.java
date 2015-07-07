package com.russellpwirtz.gcmpush;

import com.google.android.gms.iid.InstanceIDListenerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushInstanceIDListenerService extends InstanceIDListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushInstanceIDListenerService.class);

    public void onTokenRefresh() {
        LOGGER.debug("Received token refresh!");
    }

//    @Override
//    public void onTokenRefresh() {
//        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
//        Intent intent = new Intent(this, RegistrationIntentService.class);
//        startService(intent);
//    }
}
