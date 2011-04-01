package com.honeykang.browser;

import dalvik.system.VMRuntime;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class Browser extends Application {

    private final static String TAG = "Browser";
    public final static boolean DEBUG = true;
    private final static float TARGET_HEAP_UTILIZATION = 0.75f;

    public Browser(){}

    public void onCreate() {
        if (DEBUG){
            Log.d(TAG, "Browser.onCreate: this=" + this);
        }
        VMRuntime.getRuntime().setTargetHeapUtilization(
                TARGET_HEAP_UTILIZATION);
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().removeExpiredCookie();
        // TODO: Load settings
        // BrowserSettings.getInstance().loadFromDb(this);
    }

    static Intent createBrowserViewIntent() {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        return intent;
    }
}