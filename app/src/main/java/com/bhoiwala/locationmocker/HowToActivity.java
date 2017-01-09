package com.bhoiwala.locationmocker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HowToActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    DrawerLayout drawer;
    private TextView tvAbout;
    private TextView tvHowToDrop;
    private TextView tvHowToStart;
    private TextView tvHowToAddFav;
    private TextView tvOther;

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
        ab.setTitle("How to use");
        ab.setDisplayHomeAsUpEnabled(true);
        tvAbout = (TextView)findViewById(R.id.tvAbout);
        tvHowToDrop = (TextView)findViewById(R.id.tvDropMarker);
        tvHowToStart = (TextView)findViewById(R.id.tvAboutStart);
        tvHowToAddFav = (TextView)findViewById(R.id.tvAboutFav);
        tvOther = (TextView)findViewById(R.id.tvOther);
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view2);
        navigationView.setNavigationItemSelectedListener(this);


        displayInstructions();
    }

    private void displayInstructions() {
        tvAbout.setText("This app let's you mock your current location in real time.");

        tvHowToDrop.setText("Drop a marker by searching for a place using the Search bar or by " +
                "Long-Pressing anywhere on the map or by choosing to Enter Coordinates from the Navigation drawer.");

        tvHowToStart.setText("Press the Green Start button to start mocking. To stop, simply press the" +
                " Red Stop button. Location should reset in about 15 seconds.");

        tvHowToAddFav.setText("Press the Heart button in the search bar to add the place to your " +
                "Favorites list. To remove an item, press the Heart button again or Long-Press the item in the list.");

        tvOther.setText("If you have any feedback or would like to suggest a feature, please leave" +
                " your comments in the play store.");


        Button settingBtn = (Button)findViewById(R.id.mockSettingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                Toast.makeText(getApplicationContext(), "Allow this app to mock your location", Toast.LENGTH_LONG).show();
            }
        });
//        tvHowToDrop.setText("Using this app is very easy.\n" +
//                "- Enable MOCK LOCATIONS under Settings\n" +
//                "- Drop a marker\n" +
//                "- Press the START button\n" +
//
//
//                "You can choose a location by Long Pressing a location on a map or by choosing a place" +
//                "from the search bar. Alternatively, you can also enter coordinates of a location by" +
//                "choosing that option from the navigation drawer.\n\n" +
//                "If there is a place that you mock frequently, you can add it to your Favorites list " +
//                "by simply clicking the Heart button in the search bar. You can delete that item by " +
//                "clicking the Heart button again or by Long Pressing the item in the list.\n\n" +
//                "NOTE: In order to start mocking, you will have to enable this app to Mock your location. " +
//                "You can do so by going to SETTINGS -> DEVELOPER OPTIONS -> MOCK LOCATION APP\n" +
//                "Also, if you would like this app to use your current location, make sure you ALLOW the " +
//                "permission and turn on GPS.\n\n" +
//                "I created this app as a learning experience and I do not intent to make money off of it. " +
//                "And hence I have kept this app very neat, feature-rich, and completely ad-free. I would " +
//                "really appreciate if you can Rate this app in the Play store and don't forget to share " +
//                "it with your friend :) ");

    }


    public void toast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_home){
//            toast("Home");
            Intent intent = new Intent (HowToActivity.this, MapsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_howto){
//            toast("How To");
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_enterCoordinates){
            toast("Go to main page and try again");
        } else if(id == R.id.nav_satellite){
            toast("Unable to change the view from this page");
        } else if (id == R.id.nav_favorites) {
//            toast("Favorites");
            Intent intent = new Intent(HowToActivity.this, MyListViewActivity.class);
            intent.putExtra("from_id", MyStrings.favID);
            startActivity(intent);
        } else if (id == R.id.nav_recent) {
//            toast("Recent");
            Intent intent = new Intent(HowToActivity.this, MyListViewActivity.class);
            intent.putExtra("from_id", MyStrings.recID);
            startActivity(intent);
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
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
}
