package com.bignerdranch.android.remindme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

/**
 * Created by annika on 2017-08-09.
 */


/**
 * NewReminderFragment simply prompts the user for information when creating a new Reminder.
 * Doing it by showing AlertDialogs and an instance of Google Play Services PlacePicker.
 */
public class NewReminderFragment extends ServiceControllerFragment {

    private ReminderCreator reminderCreator;
    public static final int PLACE_PICKER_REQUEST = 199;
    private String dialogInputString;
    private ArrayList<String> dialogInputList = new ArrayList<>();
    private int status;

    /**
     * An interface for communicating with the calling Activity(AppActivity)
     * to be able to create a new reminder.
     *
     */
    interface ReminderCreator {
        void createReminder(Location l, String n, String s, ArrayList<String> list);
    }

    /**
     * Makes sure that the calling Activity implements the ReminderController interface.
     * @param a the calling activity.
     */
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);

        try {
            reminderCreator = (ReminderCreator) a;

        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " does not implement ReminderCreator interface");
        }
    }

    /**
     * Initial creation of the Fragment.
     * @param savedInstanceState Holds the state of the Fragment if the Fragment
     *                           is being re-created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflates the fragment with it's view and calls showInputDialog().
     * @param inflater used to inflate a layout object.
     * @param container the parent view.
     * @param savedInstanceState can store previous states of the fragment.
     * @return the view of the Fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.map_layout_view, container, false);

        showInputDialog();

        return view;
    }

    /**
     * Launch the Google Play Services PlacePicker.
     */
    private void launchPlacePicker() {
        status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (status == ConnectionResult.SUCCESS) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);

            } catch (GooglePlayServicesRepairableException e) {

                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleListInput(String s) {
        if (s == null) {
            return;
        }
        String[] chopped = s.split("\n");
        if (chopped.length == 0) {
            return;
        }
        for (int i = 0; i < chopped.length; i++) {
            dialogInputList.add(chopped[i]);
        }
        System.out.println(dialogInputList);

    }

    private void showListInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.list_input_dialog, null);
        builder.setView(dialogView);

        final EditText editText = dialogView.findViewById(R.id.text_input);

        builder.setTitle("Define a list");
        builder.setMessage("Separate your list items with new lines");
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleListInput(editText.getText().toString());

                launchPlacePicker();
            }
        });

        builder.setNegativeButton("ABOUT LIST", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                launchPlacePicker();
            }
        });

        builder.show();
    }

    /**
     * Builds and shows an input dialog where the user can type
     * the text for the reminder being created.
     */
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText inputField = new EditText(getContext());
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setTitle(R.string.input_dialog_title);
        builder.setView(inputField);
        builder.setCancelable(false);

        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInputString = inputField.getText().toString();
                launchPlacePicker();
            }
        });

//        builder.setNeutralButton("ADD LIST", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInputString = inputField.getText().toString();
//                showListInputDialog();
//            }
//        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                getActivity().getFragmentManager().popBackStack();
            }
        });

        builder.show();
    }

    /**
     * Called when the PlacePicker exits.
     * @param requestCode a request code to see where the result came from.
     * @param resultCode the result of the operation.
     * @param data the intent with the Place being picked.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                Place place = PlacePicker.getPlace(data, getActivity());
                showConfirmationDialog(place);
            }
        }
    }

    /**
     * Called after a place has been picked. Builds an Alert Dialog
     * so the user can confirm the data before a new Reminder is being created.
     * @param place the place that the user picked.
     */
    private void showConfirmationDialog(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(buildMessageString(place)).setTitle(R.string.confirmation_dialog_title);
        builder.setCancelable(false);

        final String name = (String)place.getName();
        final Location location = new Location(place.getName()+"");
        location.setLatitude(place.getLatLng().latitude);
        location.setLongitude(place.getLatLng().longitude);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                reminderCreator.createReminder(location, dialogInputString, name, dialogInputList);
                dialogInputString = "";
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInputString = "";
            }
        });

        builder.show();
    }

    /**
     * Builds a string to be sent to a confirmation alert dialog.
     * @param place the place that the user picked.
     * @return A string questioning if the Reminder text and the Place is ok.
     */
    private String buildMessageString(Place place) {
        String message = String.format("%s at %s", dialogInputString, place.getName());
        return message;
    }

    /**
     * Makes sure the dataInputString is cleaned up when Fragment is being destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        dialogInputString = "";
    }
}