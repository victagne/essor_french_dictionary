package com.helena.essorfrench;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


public class CustomArrayAdapter extends ArrayAdapter<String> {
    public CustomArrayAdapter(Context context, ArrayList<String> words) {
        super(context, 0, words);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String word = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.word_list_item, parent, false);
        }
        // Lookup view for data population
        TextView  tvWord = convertView.findViewById(R.id.word);
        // Populate the data into the template view using the data object
        tvWord.setText(word);
        // Return the completed view to render on screen
        return convertView;
    }
}
