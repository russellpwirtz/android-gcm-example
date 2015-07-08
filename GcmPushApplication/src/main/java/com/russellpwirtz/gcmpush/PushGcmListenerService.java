package com.russellpwirtz.gcmpush;

import android.os.Bundle;
import com.google.android.gms.gcm.GcmListenerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushGcmListenerService extends GcmListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushGcmListenerService.class);

    @Override
    public void onMessageReceived(String senderId, Bundle data) {
        LOGGER.error("Received message! "+ senderId + ", " + data);
        super.onMessageReceived(senderId, data);
    }
}
