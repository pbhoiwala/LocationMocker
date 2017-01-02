package com.bhoiwala.locationmocker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

public class HowToActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    DrawerLayout drawer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_others);
        Intent intent = this.getIntent();
        String from_id = intent.getStringExtra("from_id");
        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.activity_how_to_use);
        View inflated = stub.inflate();
        ActionBar ab = getSupportActionBar();
        ab.setTitle("How to use this app");
        ab.setDisplayHomeAsUpEnabled(true);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);

        displayInstructions();
    }

    private void displayInstructions() {

    }
    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
            toast("Home");
            Intent intent = new Intent (HowToActivity.this, MapsActivityOld.class);
            startActivity(intent);
        } else if (id == R.id.nav_howto){
            toast("How To");
            drawer.closeDrawer(GravityCompat.START);
        } else if(id == R.id.nav_satellite){
            toast("Unable to change the view from this page");
        } else if (id == R.id.nav_favorites) {
            toast("Favorites");
            Intent intent = new Intent(HowToActivity.this, MyListViewActivity.class);
            intent.putExtra("from_id", "FAVORITES");
            startActivity(intent);
        } else if (id == R.id.nav_recent) {
            toast("Recent");
            Intent intent = new Intent(HowToActivity.this, MyListViewActivity.class);
            intent.putExtra("from_id", "RECENT");
            startActivity(intent);
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
        return false;
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
}
