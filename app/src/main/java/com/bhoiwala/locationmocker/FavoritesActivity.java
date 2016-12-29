package com.bhoiwala.locationmocker;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bhoiwala.locationmocker.adapters.FavoriteAdapter;
import com.bhoiwala.locationmocker.realm.Favorites;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class FavoritesActivity extends AppCompatActivity {

    final Context context = this;
    ArrayList<Favorites> listOfFavorites;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        realm = Realm.getDefaultInstance();
        refreshListOfFavorites();
    }

    private void refreshListOfFavorites() {
        RealmResults<Favorites> favorites = realm.where(Favorites.class).findAll();
        listOfFavorites = new ArrayList<>();
        for(Favorites eachPlace: favorites){
            listOfFavorites.add(eachPlace);
        }
        FavoriteAdapter listAdapter = new FavoriteAdapter(this, listOfFavorites);
        final ListView listView = (ListView)findViewById(R.id.favoriteList);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Favorites listItem = (Favorites) listView.getItemAtPosition(i);
                toast(listItem.placeName);
            }
        });
    }

    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
