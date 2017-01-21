package com.bhoiwala.locationmockerpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bhoiwala.locationmockerpro.R;

import java.util.ArrayList;

public class EmptyListAdapter extends ArrayAdapter<String> {

    public EmptyListAdapter(Context context, ArrayList<String> emptyMessage) {
        super(context, 0, emptyMessage);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String emptyMessage = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_emptylist, parent, false);
        }
        // Lookup view for data population
        TextView emptyTv = (TextView) convertView.findViewById(R.id.tvEmpty);

        emptyTv.setText(emptyMessage);
        return convertView;
    }
}
