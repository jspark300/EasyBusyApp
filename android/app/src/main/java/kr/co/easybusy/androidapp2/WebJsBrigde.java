package kr.co.easybusy.androidapp2;

import android.content.Context;
import android.webkit.JavascriptInterface;

/**
 * Created by kyung on 2017. 3. 3..
 */

public class WebJsBrigde {

    public interface OnWebListener {
        public void onUserInfo(String html);
    }

    OnWebListener listener = null;
    private Context ctx;

    public WebJsBrigde(Context ctx, OnWebListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    @JavascriptInterface
    public void onUserInfo(String info) {
        if(this.listener != null) {
            this.listener.onUserInfo(info);
        }
    }
}
