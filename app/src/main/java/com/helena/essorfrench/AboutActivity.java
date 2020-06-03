package com.helena.essorfrench;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {
    private String TAG = "AboutActivity";

    private ListView listView;
    private List<Pair> listData;
    private AboutListAdapter listAdapter;

    private int[] themes = {R.string.theme_grey, R.string.theme_indigo,
            R.string.theme_green, R.string.theme_purple,
            R.string.theme_blue_grey, R.string.theme_cyan};

    private String[] history_num_items = {"20", "50", "100", "200", "500", "1000"};

    private String[] lineSpaces;

    private PrefUtils pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_about);
        }

        pref = new PrefUtils(this);

        lineSpaces = new String[3];
        lineSpaces[0] = getString(R.string.standard);
        lineSpaces[1] = "1.5";
        lineSpaces[2] = "2.0";

        listView = findViewById(R.id.aboutList);
        listData = new ArrayList<>();
        buildListData();
        listAdapter = new AboutListAdapter(this, R.layout.about_list_item, listData);
        listView.setAdapter(listAdapter);
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

    private void buildListData(){
        String[] textSizes = {  getString(R.string.small), getString(R.string.normal),
                getString(R.string.big), getString(R.string.huge)};

        int themeIndex = Integer.parseInt(pref.getTheme());
        int lsIndex = Integer.parseInt(pref.getLineSpace());
        int textSizeIndex = Integer.parseInt(pref.getTextSize());
        int historyIndex = Integer.parseInt(pref.getHistoryMaxNumber());
        String themeName = getString(themes[themeIndex]);
        String lineSpace = lineSpaces[lsIndex];
        String textSize = textSizes[textSizeIndex];
        String historyNumber = history_num_items[historyIndex];

        listData.add(new Pair(getString(R.string.change_theme), themeName));
        listData.add(new Pair(getString(R.string.text_size), textSize));
        listData.add(new Pair(getString(R.string.line_space), lineSpace));
        listData.add(new Pair(getString(R.string.about_history),
                              getString(R.string.sub_about_history) + historyNumber));
        listData.add(new Pair(getString(R.string.night_mode), null));
        listData.add(new Pair(getString(R.string.enable_voice), null));
        listData.add(new Pair(getString(R.string.highlight_french), null));
        listData.add(new Pair(getString(R.string.google_play), null));
        //listData.add(new Pair(getString(R.string.support), null));  //delete support entry
        listData.add(new Pair(getString(R.string.usage), null));
    }
}