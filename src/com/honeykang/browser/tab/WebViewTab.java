package com.honeykang.browser.tab;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost.TabContentFactory;

import com.honeykang.browser.R;

public class WebViewTab extends Tab {

    private static Context mContext = null;
    private ScrollView scrollView = null;
    private FrameLayout mView = null;
    private WebView webView = null;
    private WebViewClient webViewClient = null;
    private WebChromeClient webChromeClient = null;
    private LayoutInflater mInflater = null;
    private LinearLayout mLayout = null;
    private LinearLayout mAddressBarLayout = null;
    private EditText mAddressBar = null;
    private ImageView mGoButton = null;
    private static final String TAG = "WebViewTab";
    public ArrayList<String> urlHistory = new ArrayList<String>();
    private Activity mActivity = null;

    public void loadUrl(String url){
        webView.loadUrl(url);
        mAddressBar.setText(url);
        mAddressBar.setEnabled(false);
    }

    public WebViewTab(Context context) {
        super(context, "Loading...");
        mActivity = (Activity) context;
        mContext = context;
        mLayout = new LinearLayout(mContext);
        mInflater = LayoutInflater.from(mContext);
        mAddressBarLayout = (LinearLayout) mInflater.inflate(
                                                   R.layout.address_bar, null);
        mAddressBar = (EditText) mAddressBarLayout.findViewById(
                                                             R.id.address_bar);
        mGoButton = (ImageView) mAddressBarLayout.findViewById(R.id.go_btn);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String url = mAddressBar.getText().toString();
                loadUrl(url);
            }
        });
        mView = new FrameLayout(mContext);
        mView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                                                    LayoutParams.FILL_PARENT));
        scrollView = new ScrollView(mContext);
        webView = new WebView(mContext);
        webViewClient = new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                       String description, String failingUrl) {
                // Handle the error
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished (WebView view, String url) {
                Log.i(TAG, "Done: " + view.getTitle());
                String title = view.getTitle();
                if (title.length() > 12){
                    title = title.substring(0, 12);
                    title += "...";
                }
                mTview.setText(title);
                urlHistory.add(url);
                mAddressBar.setEnabled(true);
            }
        };
        webChromeClient = new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress)
            {
//                mActivity.setTitle("Loading...");
//                mActivity.setProgress(progress * 100);

//                if(progress == 100)
//                    mActivity.setTitle(R.string.app_name);
            }
        };
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setBlockNetworkImage(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        scrollView.removeAllViews();
        scrollView.setFillViewport(true);
        mView.removeAllViews();
        mLayout.removeAllViews();
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.addView(mAddressBarLayout, new LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                          LinearLayout.LayoutParams.WRAP_CONTENT));
        mLayout.addView(webView, new LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                                      LinearLayout.LayoutParams.FILL_PARENT));
        scrollView.addView(mLayout);
        getTabSpec().setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                return scrollView;
            }
        });
    }

}
