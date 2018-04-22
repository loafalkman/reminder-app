package com.bignerdranch.android.remindme;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annika on 2017-08-14.
 */

/**
 * Reminder holds the information about a reminder:
 * the text message, a location and a radius for the Reminder to be triggered,
 * a locationName for a better user experience.
 */
public class Reminder implements Parcelable {

    private Location location;
    private String text;
    private ArrayList<String> list;
    private String locationName;
    private int radius;
    private boolean mapIsVisible;

    public Reminder(Location l, String t, String ln, int r, ArrayList<String> li) {
        location = l;
        text = t;
        locationName = ln;
        radius = r;
        list = li;

//        list = new ArrayList<>();
//        list.add("peppar");
//        list.add("papper");
//        list.add("ost");
    }

    /**
     * @param newLocation new Location to be set as the location.
     */
    public void setLocation(Location newLocation) {
        location = newLocation;
    }

    public double getLatitude() {
        return location.getLatitude();
    }

    public double getLongitude() {
        return location.getLongitude();
    }

    /**
     * @param newName new name to be set as locationName.
     */
    public void setLocationName(String newName) {
        locationName = newName;
    }

    /**
     * @param newText new text to be set as text.
     */
    public void setText(String newText) {
        text = newText;
    }

    public ArrayList<String> getList() {
        return list;
    }

    /**
     * @return locationName.
     */
    public String getLocationName() { return locationName; }

    /**
     * @return text.
     */
    public String getText() {
        return text;
    }


    public String listToString() {
        String s = "";

        if (list != null) {
            for (String item : list) {
                s += item + "\n";
            }
        }

        return s;
    }

    private String serializeList() {
        String s = "";

        for (String item : list) {
            s += item + ",";
        }

        return s;
    }

    /**
     * @param other a Location to compare with.
     * @return true if the other Location is located inside the
     * radius of this.locatino.
     */
    public boolean isNear(Location other) {
        if (this.location.distanceTo(other) < radius) {
            return true;
        }
        return false;
    }

    public void toggleMapVisible() {
        mapIsVisible = !mapIsVisible;
    }

    public boolean isMapIsVisible() { return mapIsVisible; }



    /**
     * The private constructor used by the Creator, that assigns the saved values to the
     * new instance.
     * @param in the Parcel holding the old state.
     */
    private Reminder(Parcel in) {
        location = Location.CREATOR.createFromParcel(in);
        radius = in.readInt();
        text = in.readString();
        locationName = in.readString();
        mapIsVisible = in.readInt() == 1;
    }

    /**
     * Makes it possible to pass more information about the class, in form of a bit mask.
     * @return a bitmask with information, 0 if none.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * This is where the Parcel object is filled with the values that needs to be saved.
     * @param parcel a Parcel object for storing data.
     * @param i describes different ways to write to the parcel.
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        location.writeToParcel(parcel, i);
        parcel.writeInt(radius);
        parcel.writeString(text);
        parcel.writeString(locationName);
        parcel.writeInt(mapIsVisible ? 1 : 0);
    }

    /**
     * For receiving the class loader in the Java Runtime Environment,
     * which can load a class dynamically during runtime. It will use the private constructor
     * that takes a Parcel instance.
     */
    public final Parcelable.Creator<Reminder> CREATOR
            = new Parcelable.Creator<Reminder>() {

        /**
         * When createFromParcel is called by the system, it takes the Parcel with all the
         * information and use the private constructor to make a new instance of Reminder that
         * mirrors the old one.
         * @param source the parcel storing the old state.
         * @return a new Reminder object similar to the old one.
         */
        @Override
        public Reminder createFromParcel(Parcel source) {
            return new Reminder(source);
        }

        /**
         * Allows an array of the class to be parcelled.
         * @param size the size of the array to be created.
         * @return An empty array with the specified length.
         */
        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    /**
     * @return the member variables as one string for internal storage.
     */
    public String serialize() {
        String lat = location.getLatitude() + "";
        String lon = location.getLongitude() + "";

        return lat + "," + lon + "," + text + "," + locationName + "," + radius + "," + serializeList() + '_';
    }
}
