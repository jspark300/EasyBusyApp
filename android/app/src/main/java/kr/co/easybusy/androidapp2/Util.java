package kr.co.easybusy.androidapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kyung on 2017. 3. 6..
 */

public class Util {

    public final static String PROPERTY_FCM_ID = "fcm_id";
    public final static String PROPERTY_USER_ID = "user_id";

    public static void postUserInfoInThread(final String userId, final String userDeviceToken) {
        Log.d(EemoApplication.TAG, "postUserInfoInThread. userId: " + userId + ", " + userDeviceToken);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Util.postUserInfo(userId, userDeviceToken);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(EemoApplication.TAG, "postUserInfoInThread failed. " + e);
                }
                Log.d(EemoApplication.TAG, "postUserInfoInThread succeed.");
            }
        }).start();
    }

    public static void postUserInfo(String userId, String userDeviceToken) throws Exception {
        HttpURLConnection conn = null;
        URL url = null;

        url = new URL("http://eemo.kr/fcm/fcm.php");
        conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        Uri.Builder builder = new Uri.Builder().appendQueryParameter("user_id", userId).appendQueryParameter("user_device_token", userDeviceToken);
        String query = builder.build().getEncodedQuery();

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(query);
        writer.flush();

        String response = readInputStreamToString(conn);
        Log.d(EemoApplication.TAG, "postUserInfoInThread response. " + response);
        writer.close();
        os.close();
        conn.connect();
    }

    private static String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        catch (Exception e) {
            result = null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                }
            }
        }
        return result;
    }

    public static void storePref(Context context, String key, String value) {
        final SharedPreferences prefs = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
        Log.d(EemoApplication.TAG, "storePref key:" + key + ", value: " + value);
    }

    public static String getPref(Context context, String key) {
        final SharedPreferences prefs = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        String value = prefs.getString(key, "");
        if (value.isEmpty()) {
            return "";
        }
        return value;
    }

    public static String verify(String str) {
        return (str == null || str.isEmpty()) ? "" : str;
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }
}
