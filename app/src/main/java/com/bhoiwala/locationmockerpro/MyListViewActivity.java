package com.bhoiwala.locationmockerpro;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bhoiwala.locationmockerpro.adapters.EmptyListAdapter;
import com.bhoiwala.locationmockerpro.adapters.MyLocationAdapter;
import com.bhoiwala.locationmockerpro.realm.MyLocation;
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
        if(listOfLocations.size() == 0){
            ArrayList<String> emptyList = new ArrayList<>();
            emptyList.add("No data to display");
            EmptyListAdapter emptyAdapter = new EmptyListAdapter(this, emptyList);
            final ListView listView = (ListView) findViewById(R.id.myList);
            listView.setAdapter(emptyAdapter);
            listView.setDivider(null);
            listView.setDividerHeight(0);
        }else {
            MyLocationAdapter listAdapter = new MyLocationAdapter(this, listOfLocations);
            final ListView listView = (ListView) findViewById(R.id.myList);
            listView.setAdapter(listAdapter);
            listView.setDividerHeight(1);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    MyLocation listItem = (MyLocation) listView.getItemAtPosition(i);
                    Intent intent = new Intent(MyListViewActivity.this, MapsActivity.class);
                    intent.putExtra("from_id", "ListViewActivity");
                    intent.putExtra("place_name", listItem.placeName);
                    intent.putExtra("place_lati", listItem.latitude);
                    intent.putExtra("place_long", listItem.longitude);
                    startActivity(intent);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                String from = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, from_id);

                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    new AlertDialog.Builder(context)
                            .setTitle("Remove place")
                            .setMessage("Are you sure you want to remove this place from " + from + "?")
                            .setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    MyLocation placeToRemove = (MyLocation) listView.getItemAtPosition(i);
                                    deleteItemFromDB(from_id, placeToRemove);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
    }

    private void deleteItemFromDB(String from_id, MyLocation placeToRemove) {
        String from = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, from_id);
        toast(placeToRemove.placeName + " removed from " + from);
        RealmResults<MyLocation> getLocation = realm.where(MyLocation.class).equalTo("id", from_id).equalTo("latitude", placeToRemove.latitude).equalTo("longitude", placeToRemove.longitude).findAll();
        realm.beginTransaction();
        getLocation.deleteAllFromRealm();
        realm.commitTransaction();
        refreshList(from_id);
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
            getIntent().removeExtra("from_id");
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
            Intent intent = new Intent (MyListViewActivity.this, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
//            super.onBackPressed();
        } else if (id == R.id.nav_howto){
            Intent intent = new Intent(MyListViewActivity.this, HowToActivity.class);
            intent.putExtra("from_id", MyStrings.howID);
            startActivity(intent);


        } else if (id == R.id.nav_enterCoordinates){
            toast("Go to main page and try again");
        } else if(id == R.id.nav_satellite){
            toast("Unable to change the view from this page");

        } else if(id == R.id.nav_favorites){
            refreshList(MyStrings.favID);
            ab.setTitle(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, MyStrings.favID));

        } else if(id == R.id.nav_recent){
            refreshList(MyStrings.recID);
            ab.setTitle(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, MyStrings.recID));

        } else if (id == R.id.nav_rate) {
//            toast("Rate");
            Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse(MyStrings.RATE_URL));
            startActivity(rate);
        } else if (id == R.id.nav_share) {
//            toast("Sharing");
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Location Mocker");
            i.putExtra(Intent.EXTRA_TEXT, MyStrings.SHARE_MSG);
            startActivity(Intent.createChooser(i, "Share via"));
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
