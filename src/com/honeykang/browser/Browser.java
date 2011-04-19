/**
 * Teamdouche
 */
package com.honeykang.browser;

import android.app.Application;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import dalvik.system.VMRuntime;

/**
 * Browser.java
 * 
 * Main application
 * 
 * @author mpb
 *
 */
public class Browser extends Application {

    private final static String TAG = "Browser";
    private final static float TARGET_HEAP_UTILIZATION = 0.75f;

    public Browser(){}

    public void onCreate() {
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