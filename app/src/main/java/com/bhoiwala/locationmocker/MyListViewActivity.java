package com.bhoiwala.locationmocker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bhoiwala.locationmocker.adapters.MyLocationAdapter;
import com.bhoiwala.locationmocker.realm.MyLocation;
import com.google.common.base.CaseFormat;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class MyListViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    final Context context = this;
    ArrayList<MyLocation> listOfLocations;
    private Realm realm;
    ActionBar ab;
    private Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_others);
        ViewStub stub = (ViewStub)findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_custom_listview);
        View inflated = stub.inflate();

        drawer = (DrawerLayout)findViewById(R.id.drawer_layout2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);


        Intent intent = this.getIntent();
        String from_id = intent.getStringExtra("from_id");
       /* ViewStub stub = (ViewStub)findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_custom_listview);
        View inflated = stub.inflate();*/


        realm = Realm.getDefaultInstance();

        ab = getSupportActionBar();
        ab.setTitle(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, from_id));
        ab.setDisplayHomeAsUpEnabled(true);




        refreshList(from_id);

    }

    public void refreshList(final String from_id){
        RealmResults<MyLocation> myLocations = realm.where(MyLocation.class).equalTo("id", from_id).findAll();
        listOfLocations = new ArrayList<>();
        for(MyLocation myLocation: myLocations){
            listOfLocations.add(myLocation);
        }
        MyLocationAdapter listAdapter = new MyLocationAdapter(this, listOfLocations);
        final ListView listView = (ListView)findViewById(R.id.myList);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MyLocation listItem = (MyLocation) listView.getItemAtPosition(i);
                toast(listItem.placeName);
                Intent intent = new Intent(MyListViewActivity.this, MapsActivityOld.class);
                intent.putExtra("from_id", "ListViewActivity");
                intent.putExtra("place_name", listItem.placeName);
                intent.putExtra("place_lati", listItem.latitude);
                intent.putExtra("place_long", listItem.longitude);
                startActivity(intent);
            }
        });
    }

    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                toggleDrawer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleDrawer(){
        if(drawer.isDrawerOpen(Gravity.LEFT)){
            drawer.closeDrawer(Gravity.LEFT);
        }else{
            drawer.openDrawer(Gravity.LEFT);
        }
    }

    /**
     * Handles back button press. If navigation drawer is open, then close the app
     * otherwise do regular back press function.
     */
    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(Gravity.LEFT)){
            drawer.closeDrawer(Gravity.LEFT);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
            toast("Home");
            Intent intent = new Intent (MyListViewActivity.this, MapsActivityOld.class);
            startActivity(intent);
        } else if (id == R.id.nav_howto){
            toast("How To");
            Intent intent = new Intent(MyListViewActivity.this, HowToActivity.class);
            intent.putExtra("from_id", "HOW_TO");
            startActivity(intent);
        } else if(id == R.id.nav_satellite){
            toast("Unable to change the view from this page");
        } else if(id == R.id.nav_favorites){
            refreshList("FAVORITES");
            ab.setTitle("Favorites");
        } else if(id == R.id.nav_recent){
            refreshList("RECENT");
            ab.setTitle("Recent");
        } else if (id == R.id.nav_rate) {
            toast("Rate");
            Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.bhoiwala.grades.gradetracker"));
            startActivity(rate);
        } else if (id == R.id.nav_share) {
            toast("Sharing");
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Location Mocker");
            String sAux = "\nHey, check out Location Mocker. This android app can mock you current location " +
                    "in real time.\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.bhoiwala.grades.gradetracker \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Share via"));
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
