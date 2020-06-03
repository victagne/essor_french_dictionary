package com.helena.essorfrench;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;


public class FavoriteListAdapter extends ArrayAdapter<String> {
    Context myContext;
    LayoutInflater inflater;
    List<String> dataList;
    private SparseBooleanArray mSelectedItemsIds;

    // Constructor for get Context and  list
    public  FavoriteListAdapter(Context context, int resourceId,  List<String> lists) {
        super(context,  resourceId, lists);
        mSelectedItemsIds = new  SparseBooleanArray();
        myContext = context;
        dataList = lists;
        inflater =  LayoutInflater.from(context);
    }

    // Container Class for item
    private class ViewHolder {
        TextView tvTitle;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder  holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.favorite_list_item, null);
            holder.tvTitle = (TextView) view.findViewById(R.id.favoriteWord);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)  view.getTag();
        }
        // Capture position and set to the  TextViews
        holder.tvTitle.setText(dataList.get(position).toString());

        return view;
    }

    @Override
    public void remove(String  object) {
        dataList.remove(object);
        notifyDataSetChanged();
    }

    // get List after update or delete
    public  List<String> getMyList() {
        return dataList;
    }

    public void  toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        notifyDataSetChanged();
    }

    // Item checked on selection
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position,  value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    // Get number of selected item
    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public  SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}