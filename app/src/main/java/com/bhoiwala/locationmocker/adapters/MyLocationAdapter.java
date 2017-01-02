package com.bhoiwala.locationmocker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bhoiwala.locationmocker.R;
import com.bhoiwala.locationmocker.realm.MyLocation;

import java.util.ArrayList;

public class MyLocationAdapter extends ArrayAdapter<MyLocation> {

    public MyLocationAdapter(Context context, ArrayList<MyLocation> myLocations) {
        super(context, 0, myLocations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MyLocation myLocation = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_custom_listview, parent, false);
        }
        // Lookup view for data population
        TextView tvPlaceName = (TextView) convertView.findViewById(R.id.tvPlaceName);
        TextView tvCoordinates = (TextView) convertView.findViewById(R.id.tvCoordinates);

        // Populate the data into the template view using the data object
        // Format items: convert to string, remove extra break-lines and round long double values
        String placeName = myLocation.placeName.replace("\n", "").replace("\r","");
        String lati = (String.valueOf(String.format("%.6g%n",myLocation.latitude))).replace("\n","").replace("\r","");
        String longi = (String.valueOf(String.format("%.6g%n",myLocation.longitude))).replace("\n","").replace("\r","");
        String coordinates = "(" + lati + ", " + longi + ")";
        tvPlaceName.setText(placeName);
        tvCoordinates.setText(coordinates);

        // Return the completed view to render on screen
        return convertView;
    }
}
