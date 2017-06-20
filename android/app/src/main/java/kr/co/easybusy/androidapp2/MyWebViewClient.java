package kr.co.easybusy.androidapp2;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static kr.co.easybusy.androidapp2.EemoApplication.TAG;

/**
 * Created by 2525j on 2017-03-26.
 */

public class MyWebViewClient extends WebViewClient {
    public static final String INTENT_PROTOCOL_START = "intent:";
    public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
    public static final String INTENT_PROTOCOL_END = ";end;";
    public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";
    private AppCompatActivity mActivity = null;

    public MyWebViewClient(AppCompatActivity context) {
        mActivity = context;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //return super.shouldOverrideUrlLoading(view, url);
        if(url.startsWith("tel:")) {
            Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(dial);
            return true;

        }
        if(url.startsWith(INTENT_PROTOCOL_START)) {
            final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
            final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
            if(customUrlEndIndex < 0) {
                return false;
            } else {
                final String customUrl = url.substring(customUrlStartIndex,customUrlEndIndex);
                try {
                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)));
                } catch (ActivityNotFoundException e) {
                    Log.d(TAG, "ActivityNotFoundException. e: " + e);
                    final int packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length()+8;
                    final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);

                    final String packageName = url.substring(packageStartIndex, packageEndIndex < 0 ? url.length() : packageEndIndex);
                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                }
                return true;
            }
        } else {
            return false;
        }
    }
}
