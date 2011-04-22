/**
 * Teamdouche
 */
package com.honeykang.browser.tab;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;

import com.honeykang.browser.BrowserActivity;
import com.honeykang.browser.R;
import com.honeykang.browser.misc.BitmapHelper;
import com.honeykang.browser.provider.BrowserProvider;

/**
 * WebViewTab.java
 * 
 * @author optedoblivion
 */
public class WebViewTab extends Tab {

    private static final String TAG = "WebViewTab";
    private Context mContext = null;
    private boolean mIsLoading = false;
    //private ErrorConsoleView mErrorConsoleView = null;
    private long mLoadStartTime = 0;
    private WebView webView = null;
    private WebViewClient webViewClient = null;
    private WebChromeClient webChromeClient = null;
    private LayoutInflater mInflater = null;
    private LinearLayout mLayout = null;
    private LinearLayout mAddressBarLayout = null;
    private TextView mAddressBar = null;
    private ImageView mGoButton = null;
    private BrowserActivity mActivity = null;
    private ImageView mFavicon = null;
    private ProgressBar mProgressBar = null;
    public ArrayList<String> urlHistory = new ArrayList<String>();

    public void loadUrl(String url){
        if (mIsLoading){ return; }
        if (!url.startsWith("http://") && !url.startsWith("https://")){
            url = "http://" + url;
        }
        webView.loadUrl(url);
        mAddressBar.setText(url);
        mAddressBar.setEnabled(false);
    }

    protected void setFavicon(Bitmap favicon){
        if (favicon != null){
            mFavicon.setImageBitmap(favicon);
         // TODO: Update bookmark Favicon
        }
    }

    protected boolean isNetworkUp(){
        ConnectivityManager cm = (ConnectivityManager) 
                       mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null){
            return info.isAvailable();
        } else {
            return false;
        }
    }

    protected void startSearch(String query, boolean select, Bundle data,
                                                               boolean global){
        if (data == null){
            data = new Bundle();
            data.putString("source", "browser-type");
        }
        // TODO: Get search engine from settings
        mActivity.startSearch(query, select, data, global);
    }

    public WebViewTab(Context context) {
        super(context, "Loading...");
        mActivity = (BrowserActivity) context;
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mAddressBarLayout = (LinearLayout) mInflater.inflate(
                                                   R.layout.address_bar, null);
        mAddressBar = (TextView) mAddressBarLayout.findViewById(
                                                             R.id.address_bar);
        mAddressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startSearch(null, true, null, false);
            }
        });
        mFavicon = (ImageView) mAddressBarLayout.findViewById(R.id.favicon);
        mProgressBar = (ProgressBar) mAddressBarLayout.findViewById(
                                                                R.id.progress);
        mLayout = (LinearLayout)mInflater.inflate(R.layout.main_webview, null);
        webView = (WebView) mLayout.findViewById(R.id.main_web_view);
        webViewClient = new WebViewClient(){
            @Override
            public void onPageStarted(WebView view,String url,Bitmap favicon){
                mIsLoading = true;
                mLoadStartTime = SystemClock.uptimeMillis();
                // TODO: Voice Search
                /**
                 * Started loading a new page.  If there was a pending message
                 * to save a screenshot then we will now take the new page and
                 * save an incorrect screenshot.  Therefore remove any pending
                 * thumbnail messages from the queue.
                 */
                // TODO: removeMessages() ??
                // TODO: Touch Icon Loader ??
                // TODO: Error Console
                setFavicon(favicon);
                CookieSyncManager.getInstance().resetSync();
                if (isNetworkUp()){
                    view.setNetworkAvailable(false);
                }
                // TODO: Perfomance stats
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                       String description, String failingUrl) {
                Log.e(TAG, "Error loading url: '" + failingUrl + "'.");
                Log.e(TAG, "Description: " + description);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished (WebView view, String url) {
                String fullTitle = view.getTitle();

                setFavicon(view.getFavicon());
                String title = fullTitle;
                if (title != null){
                    if (title.length() > 12){
                        title = title.substring(0, 12);
                        title += "...";
                    }
                    mTview.setText(title);
                }
                urlHistory.add(url);
                mAddressBar.setEnabled(true);
                mIsLoading = false;
                //TODO: view.getTouchIconUrl();
                insertHistoryItem(url, fullTitle, view.getFavicon(),
                                                       createScreenshot(view));
            }
        };
        webChromeClient = new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress){
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(progress);
                if(progress == 100)
                    mProgressBar.setVisibility(View.GONE);
                }
        };
        // TODO: Replace with settings
        // Need an observer to realize new settings.
        webView.getSettings().setPluginsEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setBlockNetworkImage(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        mLayout.removeAllViews();
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.addView(mAddressBarLayout, new LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                          LinearLayout.LayoutParams.WRAP_CONTENT));
        mLayout.addView(webView, new LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                                      LinearLayout.LayoutParams.FILL_PARENT));
        getTabSpec().setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                return (View) mLayout;
            }
        });
    }

    private void insertHistoryItem(String url, String title, Bitmap favicon,
                                                            Bitmap screenShot){
        ContentValues values = new ContentValues();
        BitmapHelper favi = new BitmapHelper(favicon);
        BitmapHelper thumb = new BitmapHelper(screenShot);
        values.put("url", url);
        values.put("title", title);
        values.put("favicon", favi.getAsByteArray());
        values.put("thumbnail", thumb.getAsByteArray());
        values.put("visits", 1);
        Date d = new Date();
        values.put("date", d.getTime());
        values.put("created", d.getTime());
        values.put("description", "");
        mContext.getContentResolver().insert(BrowserProvider.HISTORY_URI,
                                                                       values);
    }
    /**
     * Values for the size of the thumbnail created when taking a screenshot.
     * Lazily initialized.  Instead of using these directly, use
     * getDesiredThumbnailWidth() or getDesiredThumbnailHeight().
     */
    private static int THUMBNAIL_WIDTH = 0;
    private static int THUMBNAIL_HEIGHT = 0;

    /**
     * Return the desired width for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     * @param context Context for finding out the density of the screen.
     * @return int desired width for thumbnail screenshot.
     */
    /* package */ static int getDesiredThumbnailWidth(Context context) {
        if (THUMBNAIL_WIDTH == 0) {
            float density = context.getResources().getDisplayMetrics().density;
            THUMBNAIL_WIDTH = (int) (90 * density);
            THUMBNAIL_HEIGHT = (int) (80 * density);
        }
        return THUMBNAIL_WIDTH;
    }

    /**
     * Return the desired height for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     * @param context Context for finding out the density of the screen.
     * @return int desired height for thumbnail screenshot.
     */
    /* package */ static int getDesiredThumbnailHeight(Context context) {
        // To ensure that they are both initialized.
        getDesiredThumbnailWidth(context);
        return THUMBNAIL_HEIGHT;
    }

    private Bitmap createScreenshot(WebView view) {
        Picture thumbnail = view.capturePicture();
        if (thumbnail == null) {
            return null;
        }
        Bitmap bm = Bitmap.createBitmap(getDesiredThumbnailWidth(mContext),
                   getDesiredThumbnailHeight(mContext), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bm);
        int thumbnailWidth = thumbnail.getWidth();
        int thumbnailHeight = thumbnail.getHeight();
        float scaleFactorX = 1.0f;
        float scaleFactorY = 1.0f;
        if (thumbnailWidth > 0) {
            scaleFactorX = (float) getDesiredThumbnailWidth(mContext) /
                    (float)thumbnailWidth;
        } else {
            return null;
        }

        if (view.getWidth() > view.getHeight() &&
                thumbnailHeight < view.getHeight() && thumbnailHeight > 0) {
            // If the device is in landscape and the page is shorter
            // than the height of the view, stretch the thumbnail to fill the
            // space.
            scaleFactorY = (float) getDesiredThumbnailHeight(mContext) /
                    (float)thumbnailHeight;
        } else {
            // In the portrait case, this looks nice.
            scaleFactorY = scaleFactorX;
        }

        canvas.scale(scaleFactorX, scaleFactorY);

        thumbnail.draw(canvas);
        return bm;
    }
}
