package com.honeykang.browser;

import java.util.ArrayList;

import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.DownloadListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TabHost;

import com.honeykang.browser.R;
import com.honeykang.browser.tab.WebViewTab;

public class BrowserActivity extends TabActivity implements DownloadListener,
                        View.OnCreateContextMenuListener {

    private final static String TAG = "BrowserActivity";
    private final static boolean DEBUG =
                                       com.honeykang.browser.Browser.DEBUG;

    public static TabHost mTabHost = null;
    private static Context mContext = null;
    private ImageView newTabBtn = null;
    private HorizontalScrollView mTabWidgetScroller = null;
    private static ArrayList<WebViewTab> mTabList = new ArrayList<WebViewTab>();
    private static final int NEW_TAB_ID = Menu.FIRST;
    private static final int CLOSE_TAB_ID = Menu.FIRST + 1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.tab_view);
        mContext = this;
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabWidgetScroller = (HorizontalScrollView) findViewById(
                                                   R.id.tabwidget_scroll_view);
        newTabBtn = (ImageView) findViewById(R.id.new_tab_btn);
        newTabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                newTab("http://www.engadget.com");
            }
        });
        /**
         * Create initial tab
         */
        newTab("http://www.engadget.com");
    }

    private void newTab(String url){
        Log.i(TAG, "starts: " + url.startsWith("http://"));
        if (!url.startsWith("http://")){
            url = "http://" + url;
        }
        WebViewTab initTab = new WebViewTab(mContext);
        initTab.loadUrl(url);
        mTabHost.addTab(initTab.getTabSpec());
        mTabList.add(initTab);
        mTabHost.setCurrentTab(mTabList.size()-1);
        mTabWidgetScroller.fullScroll(View.FOCUS_RIGHT);
        mTabWidgetScroller.scrollBy(1000, 0);
        
    }

    private void closeCurrentTab(){
        int currentIndex = mTabHost.getCurrentTab();
        closeTab(currentIndex);
    }

    private void closeTab(int index){
        if (mTabList.size() == 1){
            finish();
        }
        mTabList.remove(index);
        mTabHost.setCurrentTab(0);
        mTabHost.clearAllTabs();
        for (WebViewTab wvt: mTabList){
            mTabHost.addTab(wvt.getTabSpec());
        }
        mTabHost.setCurrentTab(index-1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, NEW_TAB_ID, 0, "New Tab").setIcon(
                                               android.R.drawable.ic_menu_add);
        menu.add(0, CLOSE_TAB_ID, 0, "Close Tab").setIcon(
                                android.R.drawable.ic_menu_close_clear_cancel);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case NEW_TAB_ID:
                newTab("http://www.engadget.com");
                return true;
            case CLOSE_TAB_ID:
                if (mTabList.size() == 1){
                    finish();
                }
                closeCurrentTab();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDownloadStart(String arg0, String arg1, String arg2,
            String arg3, long arg4) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onCreateContextMenu(ContextMenu arg0, View arg1,
            ContextMenuInfo arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onNewIntent(Intent intent){
        handleSearchIntent(intent);
    }

    protected boolean handleSearchIntent(Intent intent){
        if (intent == null) return false;

        String url = null;
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            if (data != null) url = data.toString();
        } else if (Intent.ACTION_SEARCH.equals(action)
                || MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)
                || Intent.ACTION_WEB_SEARCH.equals(action)) {
            url = intent.getStringExtra(SearchManager.QUERY);
        }
        getCurrentTab().loadUrl(url);
        return true;
    }

    private WebViewTab getCurrentTab(){
        return mTabList.get(mTabHost.getCurrentTab());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            WebViewTab wvt = getCurrentTab();
            int histCount = wvt.urlHistory.size();
            Log.i(TAG, "History Count: " + histCount);
            int index = histCount-1;
            int lastIndex = index-1;
            if (index <= 0){
                closeCurrentTab();
                return true;
            }
            String url = wvt.urlHistory.get(lastIndex);
            wvt.urlHistory.remove(index);
            wvt.urlHistory.remove(lastIndex);
            wvt.loadUrl(url);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    } 
}
