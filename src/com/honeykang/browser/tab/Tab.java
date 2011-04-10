/**
 * 
 */
package com.honeykang.browser.tab;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.honeykang.browser.R;

/**
 * Tab.java
 * 
 * @author optedoblivion
 */
public class Tab {

    private static Context mContext = null;
    //public static TabHost mTabHost = null;
    private TabSpec mTabSpec = null;
    protected TextView mTview = null;

    /**
     * This creates a {@link android.widget.TabHost.TabSpec} that can be
     * fetched by calling getTabSpec().
     * 
     * Example Usage:
     * <code>
     * Tab tab = new Tab(mContext, new TextView(mContext),
     *                                                            "Tab Title");
     * mTabHost.addTab(tab.getTabSpec());
     * </code>
     * 
     * @param context {@link android.content.Context}
     * @param tag <code>String</code>
     */
    public Tab(final Context context, final String tag) {
        mContext = context;
        setupTab(tag);
    }

    /**
     * This creates a {@link android.widget.TabHost.TabSpec} that can be
     * fetched by calling getTabSpec().  Also lets to you specify what view
     * you want to add for content.
     * 
     * Example Usage:
     * <code>
     * Tab tab = new Tab(mContext, new TextView(mContext), "Tab Title", view);
     * mTabHost.addTab(tab.getTabSpec());
     * </code>
     * 
     * @param context {@link android.content.Context}
     * @param tag <code>String</code>
     * @param view {@link android.view.View}
     */
    public Tab(final Context context, final String tag, final View view) {
        mContext = context;
        setupTab(tag);
        mTabSpec.setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                return view;
            }
        });
    }

    /**
     * This creates a {@link android.widget.TabHost.TabSpec} that can be
     * fetched by calling getTabSpec().  Also lets to you specify what intent
     * you want to add for content.
     * 
     * Example Usage:
     * <code>
     * Tab tab = new Tab(mContext, new TextView(mContext), "Tab Title",
     *                                                                 intent);
     * mTabHost.addTab(tab.getTabSpec());
     * </code>
     * 
     * @param context {@link android.content.Context}
     * @param tag <code>String</code>
     * @param intent {@link android.content.Intent}
     */
    public Tab(final Context context, final String tag,
                                                         final Intent intent) {
        mContext = context;
        setupTab(tag);
        mTabSpec.setContent(intent);
    }

    /**
     * Provides initial tab setup
     * 
     * @param tag <code>String</code>
     */
    protected void setupTab(final String tag){
        View tabview = createTabView(mContext, tag);
        mTabSpec = new TabHost(mContext).newTabSpec(tag);
        mTabSpec.setIndicator(tabview);
        final TextView view = new TextView(mContext);
        view.setText("Please add content");
        mTabSpec.setContent(new TabContentFactory() {
            public View createTabContent(String tag) {
                return view;
            }
        });
    }

    /**
     * Creates the tab control itself
     * 
     * @param context {@link android.content.Context}
     * @param text {@link java.lang.String}
     * @return {@link android.view.View}
     */
    protected View createTabView(final Context context,
                                                           final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg,
                          null);
        TextView tv = (TextView) view.findViewById(R.id.tabs_text);
        tv.setText(text);
        mTview = tv;
        return view;
    }

    public TabSpec getTabSpec(){
        return mTabSpec;
    }
}
