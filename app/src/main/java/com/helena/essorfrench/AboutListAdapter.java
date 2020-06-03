package com.helena.essorfrench;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;

public class AboutListAdapter extends ArrayAdapter<Pair> {

    private static String TAG = "AboutListAdapter";
    private Context mContext;
    private PrefUtils prefUtils;
    private LayoutInflater inflater;
    private List<Pair> dataList;
    private Typeface font;

    private static final int INDEX_CHANGE_THEME = 0;
    private static final int INDEX_TEXT_SIZE = 1;
    private static final int INDEX_LINE_SPACE = 2;
    private static final int INDEX_HISTORY_NUM = 3;
    private static final int INDEX_NIGHT_MODE = 4;
    private static final int INDEX_TTS = 5;
    private static final int INDEX_HIGHLIGHT_FRENCH = 6;
    private static final int INDEX_GOOGLE_PLAY = 7;
    private static final int INDEX_ABOUT = 8;

    private static final int icons[] = {
                R.drawable.ic_about_theme, R.drawable.ic_about_text_size,
                R.drawable.ic_about_line_space, R.drawable.ic_about_history,
                R.drawable.ic_about_night_mode, R.drawable.ic_about_tts,
                R.drawable.ic_about_highlight, R.drawable.ic_about_google_play,
                R.drawable.ic_about_usage};

    // Constructor for get Context and  list
    public  AboutListAdapter(Context context, int resourceId,  List<Pair> lists) {
        super(context,  resourceId, lists);
        mContext = context;
        prefUtils = new PrefUtils(mContext);
        dataList = lists;
        inflater =  LayoutInflater.from(context);
        font = Typeface.createFromAsset(mContext.getAssets(), "fonts/DroidSerif-Regular.ttf");
    }

    // Container Class for item
    private class ViewHolder {
        ImageView icon;
        RelativeLayout rl_about_item;
        TextView title;
        TextView data;
        CheckBox switchCheck;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        final AboutListAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new AboutListAdapter.ViewHolder();
            view = inflater.inflate(R.layout.about_list_item, null);
            holder.icon = view.findViewById(R.id.item_icon);
            holder.rl_about_item = view.findViewById(R.id.rl_about_item);
            holder.title = view.findViewById(R.id.about_item_title);
            holder.data = view.findViewById(R.id.about_item_data);
            holder.switchCheck = view.findViewById(R.id.switchCheck);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (AboutListAdapter.ViewHolder) view.getTag();
        }

        holder.icon.setImageResource(icons[position]);

        holder.rl_about_item.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(position == INDEX_CHANGE_THEME){
                    selectThemes();
                }else if(position == INDEX_LINE_SPACE){
                    changeLineSpace();
                }else if(position == INDEX_TEXT_SIZE){
                    setTextSize();
                }else if(position == INDEX_HISTORY_NUM){
                    setHistoryNumber();
                }else if(position == INDEX_GOOGLE_PLAY){
                    openAppRating(mContext);
                }else if(position == INDEX_ABOUT){
                    showAboutDialog();
                }
            }
        });
        holder.switchCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final boolean isChecked = holder.switchCheck.isChecked();
                switch (position){
                    case INDEX_NIGHT_MODE:
                        prefUtils.setNightMode(isChecked);
                        restoreTaskStack();
                        break;
                    case INDEX_TTS:
                        prefUtils.setTTS(isChecked);
                        break;

                    case INDEX_HIGHLIGHT_FRENCH:
                        prefUtils.setHighlightFrench(isChecked);
                        break;

                    default:
                        break;
                }
            }
        });

        holder.title.setText(dataList.get(position).getTitle());
        switch (position){
            case INDEX_NIGHT_MODE:
                holder.data.setVisibility(View.GONE);
                holder.switchCheck.setVisibility(View.VISIBLE);
                holder.switchCheck.setChecked(prefUtils.getNightMode());
                break;

            case INDEX_TTS:
                holder.data.setVisibility(View.GONE);
                holder.switchCheck.setVisibility(View.VISIBLE);
                holder.switchCheck.setChecked(prefUtils.getTTS());
                break;

            case INDEX_HIGHLIGHT_FRENCH:
                holder.data.setVisibility(View.GONE);
                holder.switchCheck.setVisibility(View.VISIBLE);
                holder.switchCheck.setChecked(prefUtils.getHighlightFrench());
                break;

            case INDEX_CHANGE_THEME:
            case INDEX_LINE_SPACE:
            case INDEX_TEXT_SIZE:
            case INDEX_HISTORY_NUM:
                holder.data.setText(dataList.get(position).getData());
                holder.data.setVisibility(View.VISIBLE);
                holder.switchCheck.setVisibility(View.GONE);
                break;

            case INDEX_GOOGLE_PLAY:
            case INDEX_ABOUT:
                holder.data.setVisibility(View.GONE);
                holder.switchCheck.setVisibility(View.GONE);
                break;

            default:
                break;
        }

        return view;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void selectThemes() {
        final String[] theme_items = {
                mContext.getString(R.string.theme_grey),
                mContext.getString(R.string.theme_indigo),
                mContext.getString(R.string.theme_green),
                mContext.getString(R.string.theme_purple),
                mContext.getString(R.string.theme_blue_grey),
                mContext.getString(R.string.theme_cyan)};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.change_theme));
        builder.setSingleChoiceItems(theme_items, Integer.parseInt(prefUtils.getTheme()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                        if(item != Integer.parseInt(prefUtils.getTheme())){
                            prefUtils.setTheme("" + item);
                            prefUtils.setNightMode(false);
                            restoreTaskStack();
                        }
                    }
                });


        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void changeLineSpace() {
        final String[] line_space_items = {mContext.getString(R.string.standard), "1.5", "2.0"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.line_space));
        builder.setSingleChoiceItems(line_space_items, Integer.parseInt(prefUtils.getLineSpace()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //Log.d(TAG, "item = " + item);
                        if(item != Integer.parseInt(prefUtils.getLineSpace())){
                            prefUtils.setLineSpace("" + item);
                            if(item == 0){
                                dataList.set(INDEX_LINE_SPACE, new Pair(mContext.getString(R.string.line_space),
                                        mContext.getString(R.string.standard)));
                            }else {
                                dataList.set(INDEX_LINE_SPACE, new Pair(mContext.getString(R.string.line_space),
                                        line_space_items[item]));
                            }
                            notifyDataSetChanged();
                        }
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*private void showSupportDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.support));
        View view = inflater.inflate(R.layout.support_dialog, null);
        TextView supportText = (TextView) view.findViewById(R.id.support_text) ;
        supportText.setTypeface(font);
        //final ImageView qcImage = (ImageView) view.findViewById(R.id.qcImg);
        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }*/

    private void showAboutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        View view = inflater.inflate(R.layout.about_dialog, null);
        TextView about_dialog_view = view.findViewById(R.id.about_dialog_view);
        about_dialog_view.setText(Html.fromHtml(mContext.getString(R.string.about)));
        about_dialog_view.setTypeface(font);
        builder.setView(view);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setHistoryNumber(){
        final String[] history_num_items = {"20", "50", "100", "200", "500", "1000"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.about_history));
        builder.setSingleChoiceItems(history_num_items, Integer.parseInt(prefUtils.getHistoryMaxNumber()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        int hmn;
                        try {
                            hmn = Integer.parseInt(prefUtils.getHistoryMaxNumber());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            hmn = 200;
                        }
                        if(hmn != item){
                            prefUtils.setHistoryMaxNumber("" + item);
                            dataList.set(INDEX_HISTORY_NUM, new Pair(mContext.getString(R.string.about_history),
                                    mContext.getString(R.string.sub_about_history) +history_num_items[item]));
                            notifyDataSetChanged();
                        }
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setTextSize(){
        final String[] text_size_items = {mContext.getString(R.string.small), mContext.getString(R.string.normal),
                                            mContext.getString(R.string.big), mContext.getString(R.string.huge)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.text_size));
        builder.setSingleChoiceItems(text_size_items, Integer.parseInt(prefUtils.getTextSize()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if(item != Integer.parseInt(prefUtils.getTextSize())){
                            prefUtils.setTextSize("" + item);
                                dataList.set(INDEX_TEXT_SIZE, new Pair(mContext.getString(R.string.text_size),
                                        text_size_items[item]));
                            notifyDataSetChanged();
                        }
                        if(dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        builder.setNegativeButton(R.string.dialog_option_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void restoreTaskStack() {
        Intent intent = new Intent(mContext, MainTabActivity.class);
        intent.putExtra("restart_from_theme_setting", true);
        TaskStackBuilder.create(mContext)
                .addNextIntent(intent)
                .startActivities();
    }

    private static void openAppRating(Context context) {
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id="+appId));
            context.startActivity(webIntent);
        }
    }
}

