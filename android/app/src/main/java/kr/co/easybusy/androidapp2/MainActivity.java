package kr.co.easybusy.androidapp2;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import kr.co.easybusy.androidapp2.base.EmmoBaseActivity;

public class MainActivity extends EmmoBaseActivity {

    protected WebView _webView = null;
    private boolean doubleBackToExitPressedOnce = false;
    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private final static int FILECHOOSER_NORMAL_REQ_CODE = 1;
    private final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent != null) {
            String payload = intent.getStringExtra("payload");
            Log.d(EemoApplication.TAG, "MainActivity. getIntent: " + payload);
            if(payload != null && ! payload.isEmpty()) {
                handlePayload(payload);
            }
        }

        _webView = (WebView) findViewById(R.id.webview);
        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.getSettings().setAppCacheEnabled(false);
        _webView.setWebViewClient(new MyWebViewClient(this));
        _webView.addJavascriptInterface(new WebJsBrigde(this, new WebJsBrigde.OnWebListener() {
            @Override
            public void onUserInfo(final String info) {
                Log.d(EemoApplication.TAG, "MainActivity. javascript onUserInfo() called: " + info);

                String id = Util.verify(info);
                Util.storePref(mContext, Util.PROPERTY_USER_ID, id);
                String fcmId = Util.getPref(mContext, Util.PROPERTY_FCM_ID);
                Log.d(EemoApplication.TAG, "MainActivity. fcmId: " + fcmId);

                if(! Util.isEmpty(id) && ! Util.isEmpty(fcmId)) {
                    Util.postUserInfoInThread(id, fcmId);
                } else {
                    Log.d(EemoApplication.TAG, "MainActivity. gcmId or userId is empty.");
                }
            }
        }), "app");

        _webView.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser( ValueCallback<Uri> uploadMsg) {
                Log.d("MainActivity", "3.0 <");
                openFileChooser(uploadMsg, "");
            }
            // For Android 3.0+
            public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType) {
                Log.d("MainActivity", "3.0+");
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }
            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d("MainActivity", "4.1+");
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
                Log.d("MainActivity", "5.0+");
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_LOLLIPOP_REQ_CODE);

                return true;
            }
        });

        _webView.loadUrl("http://easybusy.co.kr");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
            if (filePathCallbackNormal == null) return ;
            Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
            filePathCallbackNormal.onReceiveValue(result);
            filePathCallbackNormal = null;
        } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
            if (filePathCallbackLollipop == null) return ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            }
            filePathCallbackLollipop = null;
        }
    }

    @Override
    protected void gcmMessageReceived(String payload) {
        super.gcmMessageReceived(payload);

        Log.d(EemoApplication.TAG, "MainActivity. gcmMessageReceived: " + payload);

        handlePayload(payload);
    }

    private void handlePayload(String payload) {

        Map<String, String> map = MainActivity.parsePayload(payload);
        String msg = Util.verify(map.get("msg"));
        final String evt = Util.verify(map.get("evt"));
        Log.d(EemoApplication.TAG, "MainActivity. parse. msg: " + msg);
        Log.d(EemoApplication.TAG, "MainActivity. parse. evt: " + evt);

        showDialog(Util.verify(msg), new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(evt.equalsIgnoreCase("calendar")) {
                    _webView.loadUrl("http://eemo.kr/calendar.php");
                }
            }
        });

    }

    protected void showDialog(String msg, String title) {
        showDialog(msg, title, null);
    }

    protected void showDialog(String msg, String title, DialogInterface.OnDismissListener listener) {
        AlertDialog dlg = new AlertDialog.Builder(this).setMessage(msg).setPositiveButton(getString(R.string.ok), null).create();
        dlg.setTitle(title);
        if(listener != null) {
            dlg.setOnDismissListener(listener);
        }
        dlg.show();
    }

    protected void showDialog(String msg) {
        showDialog(msg, getString(R.string.app_name));
    }

    protected void showDialog(String msg, DialogInterface.OnDismissListener listener) {
        showDialog(msg, getString(R.string.app_name), listener);
    }

    @Override
    public void onBackPressed() {
        if (_webView.canGoBack()) {
            _webView.goBack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back_again), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    public static Map<String, String> parsePayload(final String payload) {
        final String delimiter = ":-:";
        Map<String, String> m = new HashMap<>();
        if(! Util.isEmpty(payload)) {
            for(String chunk : payload.split(delimiter) ) {
                if(! Util.isEmpty(chunk)) {
                    String[] s = chunk.split("=");
                    if(s.length > 1) {
                        m.put(s[0], s[1]);
                    }
                }
            }
        }
        return m;
    }

}
