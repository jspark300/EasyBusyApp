package kr.co.easybusy.androidapp2.service;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import kr.co.easybusy.androidapp2.EemoApplication;
import kr.co.easybusy.androidapp2.R;
import kr.co.easybusy.androidapp2.Util;

/**
 * Created by kyung on 2017. 3. 3..
 */

public class GCMRegistrationIntentService extends IntentService {
    //Constants for success and errors
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";

    //Class constructor
    public GCMRegistrationIntentService() {
        super("");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //Registering gcm to the device
        registerGCM();
    }

    private void registerGCM() {
        //Registration complete intent initially null
        Intent registrationComplete = null;

        //Register token is also null
        //we will get the token on successfull registration
        String token = null;
        try {
            //Creating an instanceid
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

            //Getting the token from the instance id
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            //Displaying the token in the log so that we can copy it to send push notification
            //You can also extend the app by storing the token in to your server
            //Log.w("GCMRegIntentService", "token:" + token);
            String fcmId = Util.verify(token);
            Util.storePref(this, Util.PROPERTY_FCM_ID, fcmId);
            String userId = Util.getPref(this, Util.PROPERTY_USER_ID);

            if(! Util.isEmpty(userId) && ! Util.isEmpty(fcmId)) {
                Util.postUserInfoInThread(userId, fcmId);
            } else {
                Log.d(EemoApplication.TAG, "GCMService. gcmId or userId is empty.");
            }
            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

            //Putting the token to the intent
            registrationComplete.putExtra("token", token);
        } catch (Exception e) {
            //If any error occurred
            //Log.w("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


}
