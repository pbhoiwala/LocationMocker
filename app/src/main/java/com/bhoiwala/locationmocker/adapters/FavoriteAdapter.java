package com.bhoiwala.locationmocker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bhoiwala.locationmocker.R;
import com.bhoiwala.locationmocker.realm.Favorites;

import java.util.ArrayList;


public class FavoriteAdapter extends ArrayAdapter<Favorites>{

    public FavoriteAdapter(Context context, ArrayList<Favorites> favorites) {
        super(context, 0, favorites);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Favorites favorites = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_favorites, parent, false);
        }
        // Lookup view for data population
        TextView tvPlaceName = (TextView) convertView.findViewById(R.id.tvPlaceName);
        TextView tvLatitude = (TextView) convertView.findViewById(R.id.tvLati);
        TextView tvLongitude = (TextView) convertView.findViewById(R.id.tvLongi);

        // Populate the data into the template view using the data object
        tvPlaceName.setText(favorites.placeName);
        tvLatitude.setText(String.valueOf(favorites.latitude));
        tvLongitude.setText(String.valueOf(favorites.longitude));

        // Return the completed view to render on screen
        return convertView;
    }
    
}
