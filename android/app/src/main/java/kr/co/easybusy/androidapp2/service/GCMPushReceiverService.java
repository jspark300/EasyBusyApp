package kr.co.easybusy.androidapp2.service;

import com.google.android.gms.gcm.GcmListenerService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Map;

import kr.co.easybusy.androidapp2.EemoApplication;
import kr.co.easybusy.androidapp2.MainActivity;
import kr.co.easybusy.androidapp2.Util;
import kr.co.easybusy.androidapp2.R;

/**
 * Created by kyung on 2017. 3. 3..
 */

public class GCMPushReceiverService extends GcmListenerService {

    public static final String GCM_MESSAGE_SUCCESS = "GcmMessageSuccess";
    private static final long[] VIBRATE_PATTERN = new long[] { 1000, 1000, 1000};

    LocalBroadcastManager mBroadcaster = null;

    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data) {

        mBroadcaster = LocalBroadcastManager.getInstance(this);
        //Getting the message from the bundle
        Log.d(EemoApplication.TAG, "data: " + data);

        Bundle bundle = data.getBundle("notification");
        String message = "";
        if(bundle != null) {
            message = bundle.getString("message"); // fcm
        } else {
            message = data.getString("message"); // gcm
        }
        Log.d(EemoApplication.TAG, "message: " + message);

        //Displaying a notiffication with the message
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE );
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG" );
        wakeLock.acquire(3000);

        sendNotification(message);
    }

    //This method is generating a notification and displaying the notification
    private void sendNotification(String payload) {

        if(EemoApplication.isActivityVisible()) {
            Intent i = new Intent();
            i.setAction(GCMPushReceiverService.GCM_MESSAGE_SUCCESS);
            i.putExtra("payload", payload);
            mBroadcaster.sendBroadcast(i);
            return;
        }

        Map<String, String> map = MainActivity.parsePayload(payload);
        String msg = Util.verify(map.get("msg"));

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("payload", payload);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT); // FLAG_ONE_SHOT
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(msg)
            .setSound(sound)
            .setVibrate(GCMPushReceiverService.VIBRATE_PATTERN)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
    }
}
