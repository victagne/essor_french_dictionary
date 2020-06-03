package com.helena.essorfrench;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MeaningActivity extends AppCompatActivity {
    private final String TAG = "MeaningActivity";

    private TextView vWord;
    private TextView vMeaning;
    private LinearLayout ll_voice;
    private TextView vSpeak;
    private ImageView vTts;
    private String word = "";
    private String meaning = "";
    private boolean bStar;
    private boolean fromClicking;
    private MenuItem mMenuShare;
    private MenuItem mMenuStar;
    private DictDataHelper mDbHelper;
    private TextToSpeech ttsHandler;
    private PrefUtils prefUtils;
    private float lineSpaceMultiple[] = {1.2f, 1.5f, 2.0f};
    private float textSizeWord[] = {24f, 26f, 28f, 30f};
    private float textSizeMeaning[] = {15f, 18f, 20f, 24f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefUtils = new PrefUtils(this);
        prefUtils.setCustomizedTheme();

        mDbHelper = new DictDataHelper(this);
        mDbHelper.createDatabase();
        mDbHelper.open();

        setContentView(R.layout.activity_meaning);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ll_voice = findViewById(R.id.ll_voice);
        vWord = findViewById(R.id.word);
        vMeaning = findViewById(R.id.meaning);
        vSpeak = findViewById(R.id.speak);
        vTts = findViewById(R.id.tts);

        if (prefUtils.getTTS()) {
            ll_voice.setVisibility(View.VISIBLE);
        } else {
            ll_voice.setVisibility(View.GONE);
        }

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/DroidSerif-Regular.ttf");
        vWord.setTypeface(font);
        vSpeak.setTypeface(font);
        vMeaning.setTypeface(font);

        int ts = Integer.parseInt(prefUtils.getTextSize());
        vWord.setTextSize(textSizeWord[ts]);
        vSpeak.setTextSize(textSizeMeaning[ts]);
        vMeaning.setTextSize(textSizeMeaning[ts]);

        Intent i = getIntent();
        word = i.getStringExtra("word").trim();
        meaning = i.getStringExtra("meaning");
        bStar = i.getBooleanExtra("star", false);
        fromClicking = i.getBooleanExtra("from_clicking", false);

        //save to history
        prefUtils.saveWordToHistory(word);
        //change to english punctuation
        meaning = BCConvert.qj2bj(meaning);

        String lines[] = meaning.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        for (int k = 0; k < lines.length; k++) {
            sb.append(lines[k].trim());
            sb.append('\n');
        }

        if (CharacterUtils.isChinese(sb.toString())) {
            String realWord = CharacterUtils.getRightWordFromChineseMeaning(this, sb.toString());
            if (realWord != null && !realWord.trim().isEmpty()) {
                word = realWord.trim();
            }
        }
        vWord.setText(word);
//        String meaning = sb.toString();
//        meaning = meaning.replaceAll("[~～]", word);
        boolean highlight = prefUtils.getHighlightFrench();
        if (highlight) {
            setTextColor(meaning);
        } else {
            vMeaning.setText(meaning);
        }
        vMeaning.setLineSpacing(0, lineSpaceMultiple[Integer.parseInt(prefUtils.getLineSpace())]);

        vMeaning.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    final float x = motionEvent.getX();
                    final float y = motionEvent.getY();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int mOffset = vMeaning.getOffsetForPosition(x, y);
                            String wordClicked;
                            try {
                                wordClicked = CharacterUtils.findWordForRightHanded(meaning, mOffset);
                            } catch (StringIndexOutOfBoundsException e) {
                                e.printStackTrace();
                                return;
                            }

//                            if (CharacterUtils.isChinese(wordClicked)) {
//                                Log.d(TAG, "is Chinese");
//                                return;
//                            }

                            if (wordClicked.equalsIgnoreCase(vWord.getText().toString())) {
                                //Log.d(TAG, "same word");
                                return;
                            }

                            //Log.d(TAG, "wordClicked = " + wordClicked);

                            //query directly
                            Cursor cursor = mDbHelper.searchSingleWord(wordClicked);
                            if (cursor != null && cursor.getCount() > 0) {
                                //Log.d(TAG, "cursor 1");
                                launchNewMeaning(cursor, wordClicked);
                                cursor.close();
                                return;
                            }

                            String sub;

                            //query by plural mode
                            if (wordClicked.endsWith("s")) {
                                sub = wordClicked.substring(0, wordClicked.length() - 1);
                                //Log.d(TAG, "sub2 = " + sub);
                                cursor = mDbHelper.searchSingleWord(sub);
                                if (cursor != null && cursor.getCount() > 0) {
                                    launchNewMeaning(cursor, sub);
                                    cursor.close();
                                    return;
                                }
                            }

                            //query by male mode
                            if (wordClicked.endsWith("e")) {
                                sub = wordClicked.substring(0, wordClicked.length() - 1);
                                //Log.d(TAG, "sub3 = " + sub);
                                cursor = mDbHelper.searchSingleWord(sub);
                                if (cursor != null && cursor.getCount() > 0) {
                                    launchNewMeaning(cursor, sub);
                                    cursor.close();
                                    return;
                                }

                                if (Character.isLowerCase((sub.substring(0, 1).toCharArray())[0])) {
                                    sub = sub.replaceFirst(sub.substring(0, 1),
                                            sub.substring(0, 1).toUpperCase());
                                    //Log.d(TAG, "sub4 = " + sub);
                                    cursor = mDbHelper.searchSingleWord(sub);
                                    if (cursor != null && cursor.getCount() > 0) {
                                        launchNewMeaning(cursor, sub);
                                        cursor.close();
                                        return;
                                    }
                                }
                            }

                            //query by first character capitalized
                            if (wordClicked.length() > 0) {
                                sub = wordClicked.replaceFirst(wordClicked.substring(0, 1),
                                        wordClicked.substring(0, 1).toUpperCase());
                                //Log.d(TAG, "sub1 = " + sub);
                                cursor = mDbHelper.searchSingleWord(sub);
                                if (cursor != null && cursor.getCount() > 0) {
                                    launchNewMeaning(cursor, sub);
                                    cursor.close();
                                    return;
                                }
                            }

                            //query by conjugation mode
                            String conjugation = wordClicked + " @C";
                            cursor = mDbHelper.searchSingleWord(conjugation);
                            if (cursor != null && cursor.getCount() > 0) {
                                //Log.d(TAG, "cursor 3");
                                launchNewMeaning(cursor, conjugation);
                                cursor.close();
                                return;
                            }
                        }
                    }).start();
                }
                return false;
            }
        });

        if (prefUtils.getTTS()) {
            vTts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = vWord.getText().toString();
                    //Log.d(TAG, "0 - text" + text);
                    if (text.endsWith("@C")) {
                        text = text.substring(0, text.length() - 3);
                    }
                    //Log.d(TAG, "1 - text" + text);
                    ttsHandler.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            });

            ttsHandler = new TextToSpeech(MeaningActivity.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    // TODO Auto-generated method stub
                    if (status == TextToSpeech.SUCCESS) {
                        int result;
                        boolean isChineseString = CharacterUtils.isChinese(vWord.getText().toString());
                        if (isChineseString) {
                            //Log.d(TAG, "is chinese string");
                            result = ttsHandler.setLanguage(Locale.SIMPLIFIED_CHINESE);
                        } else {
                            //Log.d(TAG, "is not chinese string");
                            result = ttsHandler.setLanguage(Locale.FRENCH);
                        }
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MeaningActivity.this);
                            builder.setMessage(R.string.dialog_install_tts_data);
                            builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO  Auto-generated method stub
                                }
                            });
                            builder.setPositiveButton(R.string.dialog_option_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO  Auto-generated method stub
                                    Intent installIntent = new Intent();
                                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                    startActivity(installIntent);
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setTitle(R.string.dialog_title_confirmation);
                            alert.show();
                        }
                    } else {
                        Log.e(TAG, "TTS initialization failed");
                    }
                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(fromClicking) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDbHelper != null) {
            mDbHelper.close();
        }
        if (ttsHandler != null) {
            ttsHandler.stop();
            ttsHandler.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.word_meaning_actionbar_menu, menu);

        mMenuShare = menu.findItem(R.id.action_share);
        mMenuStar = menu.findItem(R.id.action_star);
        mMenuShare.setIcon(R.drawable.ic_action_share);

        if (bStar) {
            mMenuStar.setIcon(R.drawable.ic_action_star_on);
        } else {
            mMenuStar.setIcon(R.drawable.ic_action_star_off);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                StringBuilder sb = new StringBuilder();
                sb.append(vWord.getText());
                sb.append('\n');
                sb.append('\n');
                sb.append(vMeaning.getText());
                sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_to)));

                return true;

            case R.id.action_star:
                bStar = !bStar;
                if (bStar) {
                    mMenuStar.setIcon(R.drawable.ic_action_star_on);
                } else {
                    mMenuStar.setIcon(R.drawable.ic_action_star_off);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mDbHelper.updateStar(word, bStar);
                    }
                }).start();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchNewMeaning(Cursor cursor, String word) {
        String realWord = word;
        String meaning = cursor.getString(0);
        boolean star = cursor.getInt(1) > 0;

        launch(realWord, meaning, star);
    }

    private void launch(String word, String meaning, boolean star) {
        Intent i = new Intent(MeaningActivity.this, MeaningActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra("word", word);
        i.putExtra("meaning", meaning);
        i.putExtra("star", star);
        i.putExtra("from_clicking", true);
        startActivity(i);
        overridePendingTransition(0, 0);
    }

    private void setTextColor(String meaning) {
        SpannableStringBuilder sb = new SpannableStringBuilder(meaning);
        Pattern p = Pattern.compile(getString(R.string.highlight_matcher), Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(meaning);
        int color = Color.rgb(67, 165, 207);
        while (m.find()) {
            sb.setSpan(new ForegroundColorSpan(color), m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        vMeaning.setText(sb);
    }
}
