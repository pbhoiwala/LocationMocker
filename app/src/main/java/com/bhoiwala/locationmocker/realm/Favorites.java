package com.bhoiwala.locationmocker.realm;
import io.realm.RealmObject;

public class Favorites extends RealmObject{
    public String placeName;
    public double latitude, longitude;
}
