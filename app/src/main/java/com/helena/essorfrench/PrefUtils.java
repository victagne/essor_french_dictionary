package com.helena.essorfrench;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class PrefUtils {

    private static final String HISTORY_SEPARATOR = ";";
    private static final String PREF_DICTIONARY = "dict_history";
    private static final String HISTORY_KEY = "history";
    private static final String THEME_KEY = "theme";
    private static final String TTS_KEY = "tts";
    private static final String HIGHLIGHT_KEY = "highlight_french";
    private static final String NIGHT_MODE_KEY = "night_mode";
    private static final String LINE_SPACE_KEY = "line_space";
    private static final String TEXT_SIZE_KEY = "text_size";
    private static final String HISTORY_NUMBER_KEY = "history_number";
    private static final String DEFAULT_HISTORY_NUMBER = "3";  //200

    private static final String DEFAULT_THEME = "1";  //indigo
    private static final String DEFAULT_LINE_SPACE = "0";  //standard
    private static final String DEFAULT_TEXT_SIZE = "1";   //18sp

    private int[] themes = {R.style.AppThemeGrey, R.style.AppThemeIndigo,
                            R.style.AppThemeGreen, R.style.AppThemePurple,
                            R.style.AppThemeBlueGrey, R.style.AppThemeCyan};

    private Context context;

    public PrefUtils(Context context) {
        this.context = context;
    }

    public void saveWordToHistory(String word){
        final String[] history_num_items = {"20", "50", "100", "200", "500", "1000"};
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String history = pref.getString(HISTORY_KEY, "");
        String strTemp;
        int index = Integer.parseInt(getHistoryMaxNumber());
        int MAX_HISTORY = Integer.parseInt(history_num_items[index]);
        //Log.d("PrefUtils", "MAX_HISTORY = " + MAX_HISTORY);

        if(!isWordSaved(history, word)) {
            StringBuilder sb;
            String[] arrayHistory = history.split(HISTORY_SEPARATOR);
            if(arrayHistory.length == MAX_HISTORY){
                sb = new StringBuilder();
                for(int i=0; i<MAX_HISTORY-1; i++){
                    sb.append(arrayHistory[i] + HISTORY_SEPARATOR);
                }
            }else{
                sb = new StringBuilder(history);
            }
            sb.insert(0, word + HISTORY_SEPARATOR);
            strTemp = sb.toString();
        }else{
            strTemp = moveWordToFirst(history, word);
        }
        editor.putString(HISTORY_KEY, strTemp);
        editor.commit();
    }

    public void removeWordFromHistory(String word){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String history = pref.getString(HISTORY_KEY, "");
        String[] arrayHistory = history.split(HISTORY_SEPARATOR);
        String subString = "";
        for(int i=0; i<arrayHistory.length; i++){
            if(arrayHistory[i].equals(word)){
                continue;
            }
            subString += arrayHistory[i] + HISTORY_SEPARATOR;
        }
        editor.putString(HISTORY_KEY, subString);
        editor.commit();

    }

    public void clearHistory(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(HISTORY_KEY, "");
        editor.commit();
    }

    public String getHistory(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        String history = pref.getString(HISTORY_KEY, "");

        return history;
    }

    public String getTheme(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        String theme = pref.getString(THEME_KEY, DEFAULT_THEME);
        return theme;
    }

    public void setTheme(String theme){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(THEME_KEY, theme);
        editor.commit();
    }

    public String getLineSpace(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        String ls = pref.getString(LINE_SPACE_KEY, DEFAULT_LINE_SPACE);
        return ls;
    }

    public void setLineSpace(String ls){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LINE_SPACE_KEY, ls);
        editor.commit();
    }

    public String getTextSize(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        String textSize = pref.getString(TEXT_SIZE_KEY, DEFAULT_TEXT_SIZE);
        return textSize;
    }

    public void setTextSize(String textSize){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TEXT_SIZE_KEY, textSize);
        editor.commit();
    }

    public String getHistoryMaxNumber(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        String historyNum = pref.getString(HISTORY_NUMBER_KEY, DEFAULT_HISTORY_NUMBER);
        return historyNum;
    }

    public void setHistoryMaxNumber(String historyNum){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(HISTORY_NUMBER_KEY, historyNum);
        editor.commit();
    }

    public void setCustomizedTheme(){
        PrefUtils pu = new PrefUtils(context);

        if(getNightMode()){
            context.setTheme(R.style.NightTheme);
            return;
        }

        int index = Integer.parseInt(pu.getTheme());
        context.setTheme(themes[index]);
    }

    public boolean getNightMode(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        return pref.getBoolean(NIGHT_MODE_KEY, false);
    }

    public void setNightMode(Boolean isChecked){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(NIGHT_MODE_KEY, isChecked);
        editor.commit();
    }

    public boolean getTTS(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        return pref.getBoolean(TTS_KEY, true);
    }

    public void setTTS(Boolean isChecked){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(TTS_KEY, isChecked);
        editor.commit();
    }

    public boolean getHighlightFrench(){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        return pref.getBoolean(HIGHLIGHT_KEY, true);
    }

    public void setHighlightFrench(Boolean isChecked){
        SharedPreferences pref = context.getSharedPreferences(PREF_DICTIONARY,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(HIGHLIGHT_KEY, isChecked);
        editor.commit();
    }

    private boolean isWordSaved(String history, String word){
        String[] historyArray = history.split(HISTORY_SEPARATOR);
        for(int i=0; i<historyArray.length; i++){
            if(historyArray[i].equals(word))
                return true;
        }
        return false;
    }

    private String moveWordToFirst(String history, String word){
        String[] historyArray = history.split(HISTORY_SEPARATOR);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<historyArray.length; i++){
            if(historyArray[i].equals(word)) {
                continue;
            }
            sb.append(historyArray[i] + HISTORY_SEPARATOR);
        }
        return sb.insert(0, word + HISTORY_SEPARATOR).toString();
    }
}
