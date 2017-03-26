package kr.co.easybusy.androidapp2;

import android.app.Application;

/**
 * Created by kyung on 2017. 3. 3..
 */

public class EemoApplication extends Application {

    public static String TAG = "_app";

    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
}
