package com.bignerdranch.android.remindme;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by annika on 2017-08-30.
 */

/**
 * This View class holds the references to the Views and Widgets that is used to represent
 * a Reminder and edit the same.
 */
public class ReminderListItemView extends RelativeLayout implements OnMapReadyCallback {

    private View view;
    private Context context;
    private GoogleMap googleMap;
    private TextView titleView, locationView, listView;
    private LinearLayout checkListView;
    private ImageButton optionsButton;
    private Reminder reminder;
    public MapView mapView;

    public ReminderListItemView(Context context) {
        this(context, null);

    }

    public ReminderListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.reminder_view, this);
        mapView = (MapView) view.findViewById(R.id.map_view);
        mapView.getMapAsync(this);
        titleView = (TextView) view.findViewById(R.id.reminder_title_view);
        locationView = (TextView) view.findViewById(R.id.reminder_location_view);
        optionsButton = (ImageButton) view.findViewById(R.id.options_menu_button);
        listView = (TextView) view.findViewById(R.id.reminder_list_view);
        checkListView = (LinearLayout) view.findViewById(R.id.checklist_view);
    }

    /**
     * Called from MyRecyclerAdapter's onCreateViewHolder.
     * To prevent mapView to live it's own life and possibly be destroyed,
     * mapView needs to be in sync with the underlying fragment.
     */
    public void mapViewOnCreate(Bundle savedInstanceState) {
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    /**
     * Called from MyRecyclerAdapter's onBindViewHolder, via
     * ReminderHolder. To prevent mapView to live it's own life and possibly be destroyed,
     * mapView needs to be in sync with the underlying fragment.
     */
    public void mapViewOnResume() {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    /**
     * @param text sets the title of the reminder to the titleView.
     */
    public void setTitleText(String text) {
        titleView.setText(text);
        titleView.setTypeface(null, Typeface.BOLD);
    }

    /**
     * @param text sets the LocationName to the locationView.
     */
    public void setLocationNameText(String text) {
        locationView.setText(text);
    }

    public void setListText(String text) {
        listView.setText(text);


    }

    /**
     * @param r the Reminder to be displayed.
     */
    public void setReminder(Reminder r) {
        reminder = r;
    }

    /**
     * @return the optionsButton.
     */
    public ImageButton getOptionsButton() {
        return optionsButton;
    }

    /**
     * Places a pin at the location of the reminder.
     */
    public void pinMap() {
        LatLng location = new LatLng(reminder.getLatitude(), reminder.getLongitude());

        if (googleMap != null && reminder != null) {
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    /**
     * From the OnMapReadyCallback interface.
     * It is used to catch the GoogleMap that is returned when the MapView is loaded (calling
     * getMapAsync()). The googleMap object is the reference one can
     * customize the MapView with.
     * In this case it is used to place the pin at the location of the Reminder.
     * @param googleMap the GoogleMap form the MapView.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        pinMap();
    }
}
