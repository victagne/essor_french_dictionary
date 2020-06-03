package com.helena.essorfrench;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabWidget;

public class MainTabActivity extends TabActivity {

    private PrefUtils prefUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = new PrefUtils(this);
        prefUtils.setCustomizedTheme();
        setContentView(R.layout.activity_main_tabs);

        // create the TabHost that will contain the Tabs
        TabHost tabHost = findViewById(android.R.id.tabhost);
        TabWidget tabs = findViewById(android.R.id.tabs);

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third tab");
        TabHost.TabSpec tab4 = tabHost.newTabSpec("Fourth tab");

        //set each item selector
        tab1.setIndicator(null, getResources().getDrawable(R.drawable.tab_translate_selector));
        tab1.setContent(new Intent(this, MainActivity.class));

        tab2.setIndicator(null, getResources().getDrawable(R.drawable.tab_history_selector));
        tab2.setContent(new Intent(this, HistoryActivity.class));

        tab3.setIndicator(null, getResources().getDrawable(R.drawable.tab_star_selector));
        tab3.setContent(new Intent(this, FavoriteActivity.class));

        tab4.setIndicator(null, getResources().getDrawable(R.drawable.tab_about_selector));
        tab4.setContent(new Intent(this, AboutActivity.class));

        // Add the tabs to the TabHost to display
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
        tabHost.addTab(tab4);

        if(getIntent().getBooleanExtra("restart_from_theme_setting", false)){
            tabHost.setCurrentTab(3);
        }else{
            tabHost.setCurrentTab(0);
        }

        if(prefUtils.getNightMode()){
            tabs.setBackgroundColor(getResources().getColor(R.color.darkTabwigetBg));
        }
    }

    @Override
    public void onBackPressed() {

        try{
            super.onBackPressed();
        }catch (IllegalStateException e){
            // can output some information here
            finish();
        }
    }
}
