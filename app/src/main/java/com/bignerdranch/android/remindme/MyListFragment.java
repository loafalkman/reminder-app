package com.bignerdranch.android.remindme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

/**
 * Created by annika on 2017-08-09.
 */


/**
 * MyListFragment gets the current collection of Reminders in the Store, via the caller AppActivity.
 * The collection is rendered with a RecyclerView. It also handles actions from the RecyclerView,
 * when the user wants to change data in a Reminder.
 */
public class MyListFragment extends ServiceControllerFragment
        implements MyRecyclerAdapter.UserInputDelegate, View.OnClickListener {

    private Button newReminderButton;
    private ArrayList<Reminder> reminders;
    private View view;
    private RecyclerView recyclerView;
    private MyRecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String newText = "";
    private MyRecyclerAdapter.ReminderHolder currentReminderHolder;
    private NewReminderFragmentLauncher reminderFragmentLauncher;

    /**
     * Delegate interface so that MyListFragment is able to
     * launch the NewReminderFragment via AppActivity.
     */
    interface NewReminderFragmentLauncher {
        void launchNewReminderFragment();
    }

    /**
     * Makes sure that the calling Activity implements the NewFragmentLauncher interface.
     * @param a the calling activity.
     */
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);

        try {
            reminderFragmentLauncher = (MyListFragment.NewReminderFragmentLauncher) a;

        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " does not implement NewReminderFragmentLauncher interface");
        }
    }

    /**
     * Initial creation of the Fragment. Gets the reminders sent by AppActivity.
     * @param savedInstanceState Holds the state of the Fragment if the Fragment
     *                           is being re-created.
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        reminders = bundle.getParcelableArrayList(AppActivity.KEY_REMINDERS);
    }

    /**
     * Inflates the fragment with it's view and calls initializeRecyclerView().
     * @param inflater used to inflate a layout object.
     * @param container the parent view.
     * @param savedInstanceState can store previous states of the fragment.
     * @return the view of the Fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.list_fragment_view, container, false);
        newReminderButton = (Button) view.findViewById(R.id.new_reminder_button);
        newReminderButton.setOnClickListener(this);

        setButtonVisibility();
        initializeRecyclerView();

        return view;
    }

    /**
     * The newReminderButton will only be visible
     * if there are no Reminders added. Just to fill out
     * the empty space and encourage the user to add a reminder.
     */
    @Override
    public void setButtonVisibility() {
        if (reminders.isEmpty()) {
            newReminderButton.setVisibility(View.VISIBLE);

        } else {
            newReminderButton.setVisibility(View.GONE);
        }
    }

    /**
     * Sets everything up for the recycler view using the custom MyRecyclerAdapter.
     */
    @Override
    public void initializeRecyclerView() {
        recyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyRecyclerAdapter(reminders, getContext(), (MyRecyclerAdapter.UserInputDelegate) this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * When Fragment being stopped, saves the reminders inside the bundle to
     * be re-created again.
     * @param savedInstanceState bundle to save data in.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelableArrayList(AppActivity.KEY_REMINDERS, reminders);
    }

    /**
     * Tells the Fragment when it is fully associated with the
     * calling activity and data can be re-stored.
     * @param savedInstanceState saved data.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            reminders = savedInstanceState.getParcelableArrayList(AppActivity.KEY_REMINDERS);
        }
    }

    /**
     * Clean up.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        currentReminderHolder = null;
    }

    /**
     * Launch the Google Play Services PlacePicker for chosing a new Place.
     */
    @Override
    public void pickNewPlace(MyRecyclerAdapter.ReminderHolder holder) {
        currentReminderHolder = holder;
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (status == ConnectionResult.SUCCESS) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(getActivity()), NewReminderFragment.PLACE_PICKER_REQUEST);

            } catch (GooglePlayServicesRepairableException e) {

                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called after a place has been picked. Builds an Alert Dialog
     * so the user can confirm the data before the Reminder is being updated.
     * @param place the place that the user picked.
     */
    private void showConfirmationDialog(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirmation_dialog_new_place);

        builder.setCancelable(false);

        final String name = (String)place.getName();
        final Location location = new Location(place.getName()+"");
        location.setLatitude(place.getLatLng().latitude);
        location.setLongitude(place.getLatLng().longitude);

        builder.setMessage("Change location to " + name + "?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentReminderHolder.reminder.setLocationName(name);
                currentReminderHolder.reminder.setLocation(location);
                currentReminderHolder = null;
                initializeRecyclerView();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentReminderHolder = null;
            }
        });

        builder.show();
    }

    /**
     * Builds and shows an input dialog where the user can type
     * the new text for the reminder held by currentReminderHolder.
     */
    @Override
    public void editText(MyRecyclerAdapter.ReminderHolder holder) {

        final MyRecyclerAdapter.ReminderHolder currentHolder = holder;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText inputField = new EditText(getContext());
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setTitle(R.string.input_dialog_new_title);
        builder.setView(inputField);
        builder.setCancelable(false); // hmmm

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                newText = inputField.getText().toString();
                currentHolder.reminder.setText(newText);
                initializeRecyclerView();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    /**
     * When a change is being made from the RecyclerAdapter,
     * the appActivity needs to be notified about the change.
     * In particular, the CurrentLocationService might have to start or stop
     * depending on the amount of Reminders.
     */
    @Override
    public void notifyActivity() {
        serviceController.serviceControl();
    }

    /**
     * Called when the PlacePicker exits.
     * @param requestCode a request code to see where the result came from.
     * @param resultCode the result of the operation.
     * @param data the intent with the Place being picked.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NewReminderFragment.PLACE_PICKER_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                Place place = PlacePicker.getPlace(data, getActivity());
                showConfirmationDialog(place);
            }
        }
    }

    /**
     * OnClickListener for the newReminderButton.
     * @param view the root view.
     */
    @Override
    public void onClick(View view) {
        reminderFragmentLauncher.launchNewReminderFragment();
    }
}