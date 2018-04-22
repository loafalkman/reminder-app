package com.bignerdranch.android.remindme;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by annika on 2017-08-14.
 */

/**
 * A custom wrapper class for the ArrayList holding the Reminders.
 */
public class Store implements Parcelable {

    private ArrayList<Reminder> reminders;
    public static final int MAX_DISTANCE = 75;

    public Store() {
        reminders = new ArrayList<>();
    }

    /**
     * The private constructor used by the Creator, that assigns the saved values to the
     * new instance.
     * @param in the Parcel holding the old state.
     */
    private Store(Parcel in) {
        in.readTypedList(reminders, null);
    }

    /**
     * Adds a new Reminder to the store.
     * @param r new Reminder/
     * @return the new Reminder if it wasn't already added.
     */
    public Reminder add(Reminder r) {
        if (!reminders.contains(r)) {
            reminders.add(r);
            return r;

        } else {
            return null;
        }
    }

    /**
     * @return the reminders.
     */
    public ArrayList<Reminder> getReminders() {
        return reminders;
    }

    /**
     * @param i an index
     * @return the Reminder located at the specified index.
     */
    public Reminder getIndex(int i) {
        return reminders.get(i);
    }

    /**
     * Iterates over the collection of Reminders, to see if
     * one is close to the Location.
     * @param location A Location.
     * @return the index of a Reminder close to the Location.
     */
    public int isNear(Location location) {

        for (Reminder reminder : reminders) {
            if (reminder.isNear(location)) {
                return reminders.indexOf(reminder);
            }
        }

        return -1;
    }

    /**
     * @return true if there are no reminders in store.
     */
    public boolean isEmpty() {
        return reminders.isEmpty();
    }

    /**
     * Removes a Reminder based on it's index.
     * @param index the index.
     */
    public void remove(int index) {
        reminders.remove(index);
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
        parcel.writeTypedList(reminders);
    }

    /**
     * For receiving the class loader in the Java Runtime Environment,
     * which can load a class dynamically during runtime. It will use the private constructor
     * that takes a Parcel instance.
     */
    public final Parcelable.Creator<Store> CREATOR = new Parcelable.Creator<Store>() {
        /**
         * When createFromParcel is called by the system, it takes the Parcel with all the
         * information and use the private constructor to make a new instance of Store that
         * mirrors the old one.
         * @param source the parcel storing the old state.
         * @return a new Store object similar to the old one.
         */
        @Override
        public Store createFromParcel(Parcel source) {
            return new Store(source);
        }

        /**
         * Allows an array of the class to be parcelled.
         * @param size the size of the array to be created.
         * @return An empty array with the specified length.
         */
        @Override
        public Store[] newArray(int size) {
            return new Store[size];
        }
    };

    /**
     * Assembles the Reminders serialized strings into one to be saved to file.
     * @return the string containing all the reminders serialized strings.
     */
    public String serialize() {
        String out = "";
        for (Reminder r : reminders) {
            out += r.serialize();
        }

        return out;
    }

    /**
     * Chops the input string and create new Reminder objects from the data.
     * @param input string with data stored in the device.
     */
    public void deSerialize(String input) {

        String[] rows = input.split("_");
        Location location;
        ArrayList<String> list;
        String[] listContent;

        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            location = new Location("");
            location.setLatitude(Double.parseDouble(cols[0]));
            location.setLongitude(Double.parseDouble(cols[1]));
            list = new ArrayList<>();
            if (cols.length > 5) {
                listContent = cols[5].split("^");
                System.out.println("listContent: "+ listContent);

                for (int j = 5; j < cols.length; j++) {
                    list.add(cols[j]);
                }
            }

            reminders.add(new Reminder(location, cols[2], cols[3], Integer.parseInt(cols[4]), list));
        }
    }
}
