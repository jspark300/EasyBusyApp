package kr.co.easybusy.androidapp2;

import android.content.Context;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 *
 */
final class EventWebChromeClient extends WebChromeClient {

    public interface PopupEventListener {
        boolean onJsAlert(WebView view, String url, String message, JsResult result);
        boolean onJsConfirm(WebView view, String url, String message, JsResult result);
        boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);
    }

    private Context context = null;
    private PopupEventListener mPopupEventListener = null;

    public EventWebChromeClient(Context context, PopupEventListener listener) {
        this.context = context;
        this.mPopupEventListener = listener;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        result.cancel();

        if(this.mPopupEventListener != null) {
            this.mPopupEventListener.onJsAlert( view,  url,  message,  result);
        }
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        result.cancel();
        if(this.mPopupEventListener != null) {
            this.mPopupEventListener.onJsConfirm( view,  url,  message,  result);
        }
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        result.cancel();
        if(this.mPopupEventListener != null) {
            this.mPopupEventListener.onJsPrompt( view,  url,  message,  defaultValue, result);
        }
        return true;
    }

}