package kr.co.easybusy.androidapp2.base;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import kr.co.easybusy.androidapp2.EemoApplication;
import kr.co.easybusy.androidapp2.R;
import kr.co.easybusy.androidapp2.service.GCMPushReceiverService;
import kr.co.easybusy.androidapp2.service.GCMRegistrationIntentService;

/**
 * Created by kyung on 2017. 3. 3..
 */

public class EmmoBaseActivity extends AppCompatActivity {

    protected BroadcastReceiver mRegistrationBroadcastReceiver;
    protected Context mContext = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        //Initializing our broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            //When the broadcast received
            //We are sending the broadcast from GCMRegistrationIntentService

            @Override
            public void onReceive(Context context, Intent intent) {

                //If the broadcast has received with success
                //that means device is registered successfully
                if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)){
                    //Getting the registration token from the intent
                    String token = intent.getStringExtra("token");
                    //Displaying the token as toast
                    Log.d(EemoApplication.TAG, "Registration token: " + token);

//                    Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();
                    //if the intent is not with success then displaying error messages
                } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)){
                    Log.d(EemoApplication.TAG, "GCM Registration failed.");
                    Toast.makeText(getApplicationContext(), getString(R.string.gcm_register_failed), Toast.LENGTH_LONG).show(); // GCM registration error!
                } else if(intent.getAction().equals(GCMPushReceiverService.GCM_MESSAGE_SUCCESS)){
                    String payload = intent.getStringExtra("payload");
                    gcmMessageReceived(payload);
                    Log.d(EemoApplication.TAG, "GCM message success!. payload: " + payload);
//                    Toast.makeText(getApplicationContext(), "GCM message success!: " + payload, Toast.LENGTH_LONG).show();
                } else {
                    Log.d(EemoApplication.TAG, "Error occurred.");
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                }
            }
        };

        //Checking play service is available or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        //if play service is not available
        if(ConnectionResult.SUCCESS != resultCode) {
            //If play service is supported but not installed
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Displaying message that play service is not installed
                Toast.makeText(getApplicationContext(), getString(R.string.google_play_is_not_installed), Toast.LENGTH_LONG).show();
                //Google Play Service is not install/enabled in this device!
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.google_play_not_supported), Toast.LENGTH_LONG).show();
                //"This device does not support for Google Play Service!"
            }

            //If play service is available
        } else {
            //Starting intent to register device
            Intent itent = new Intent(this, GCMRegistrationIntentService.class);
            startService(itent);
        }
    }

    protected void gcmMessageReceived(String payload) {
    }

    //Registering receiver on activity resume
    @Override
    protected void onResume() {
        super.onResume();
        EemoApplication.activityResumed();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
            new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
            new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
            new IntentFilter(GCMPushReceiverService.GCM_MESSAGE_SUCCESS));
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        EemoApplication.activityPaused();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
}
