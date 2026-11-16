package com.pocketledger.local;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String PREFS = "pocketledger_native";
    private static final String QUEUE = "pending_notifications";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(15, 23, 42));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(15, 23, 42));
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) { drainQueue(); }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void drainQueue() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(QUEUE, "[]");
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(QUEUE, "[]").apply();
        try {
            JSONArray items = new JSONArray(raw);
            for (int i = 0; i < items.length(); i++) {
                String text = items.getString(i);
                String js = "window.receiveAndroidNotification && window.receiveAndroidNotification(" + JSONObject.quote(text) + ");";
                webView.evaluateJavascript(js, null);
            }
        } catch (Exception ignored) { }
    }

    public class AndroidBridge {
        @JavascriptInterface public void openNotificationSettings() {
            runOnUiThread(() -> {
                try { startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); }
                catch (Exception e) { startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")); }
            });
        }
        @JavascriptInterface public boolean isNotificationAccessEnabled() {
            String enabled = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
            return enabled != null && enabled.contains(getPackageName());
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (webView != null) webView.postDelayed(() -> {
            webView.evaluateJavascript("window.refreshAndroidAccessButton && window.refreshAndroidAccessButton();", null);
            drainQueue();
        }, 350);
    }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
