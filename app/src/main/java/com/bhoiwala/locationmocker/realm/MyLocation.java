package com.bhoiwala.locationmocker.realm;

import io.realm.RealmObject;

public class MyLocation extends RealmObject {
    public String id; // id will hold where MyLocation is from Favorites or Recent
    public String placeName;
    public double latitude, longitude;
}
