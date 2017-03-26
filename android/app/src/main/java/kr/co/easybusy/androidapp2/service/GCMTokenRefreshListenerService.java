package kr.co.easybusy.androidapp2.service;

import com.google.android.gms.iid.InstanceIDListenerService;

import android.content.Intent;

/**
 * Created by kyung on 2017. 3. 3..
 */

public class GCMTokenRefreshListenerService extends InstanceIDListenerService {

    //If the token is changed registering the device again
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }
}
