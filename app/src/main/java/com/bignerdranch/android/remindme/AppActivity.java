package com.bignerdranch.android.remindme;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * The actual root activity of the app. Controls the Store, the App bar and it's Fragments,
 * the LocationUpdateService and push notifications.
 */
public class AppActivity extends AppCompatActivity
        implements NewReminderFragment.ReminderCreator,
                    ServiceControllerFragment.ServiceController,
                    MyListFragment.NewReminderFragmentLauncher {

    private static final String FILENAME = "remindme_data";
    public static final String LOCATION_UPDATE = "location update";
    public static final String LOCATION = "location";
    public static final String KEY_STORE = "store";
    public static final String KEY_REMINDERS = "reminders";
    public static final String KEY_CURRENT_FRAGMENT = "current fragment";

    private static final int NOTIFICATION_ID = 321123;

    Intent locationService;

    private BroadcastReceiver broadcastReceiver;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private Store store;

    /**
     * Creates an instance of CurrentLocationService to be used if/when
     * there are Reminders to keep track of.
     * Creates a FragmentManager to handle the Fragments.
     * Sets up the Toolbar and calls initializeState.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_view);

        locationService = new Intent(this, CurrentLocationService.class);
        fragmentManager = getSupportFragmentManager();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        FrameLayout fragmentFrame = (FrameLayout) findViewById(R.id.content_fragment);
        initializeState(savedInstanceState);
    }

    /**
     * Initializing the state of the app.
     * @param savedInstanceState Bundle with data store in.
     */
    private void initializeState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            store = savedInstanceState.getParcelable(KEY_STORE);
            currentFragment =  getSupportFragmentManager().getFragment(savedInstanceState, KEY_CURRENT_FRAGMENT);
            if (currentFragment != null) {
                launchListFragment();
            }

        } else {
            store = new Store();

            String data = loadData();
            if (!data.isEmpty()) {
                store.deSerialize(data);
            }

            launchListFragment();
        }
    }

    /**
     * Writes the data of all stored Reminders to file.
     */
    private void saveData() {
        FileOutputStream outputStream = null;

        try {
            outputStream = openFileOutput(FILENAME, Context.MODE_PRIVATE);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            outputStream.write(store.serialize().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the stored data, if any, from file into a String.
     * @return a string with the data.
     */
    private String loadData() {
        StringBuilder stringBuilder = new StringBuilder();
        FileInputStream fis;

        try {
            fis = openFileInput(FILENAME);

            if (fis != null) {
                InputStreamReader streamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                String nextLine = null;

                while ((nextLine = bufferedReader.readLine())!= null) {
                    stringBuilder.append(nextLine);
                }
                fis.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    /**
     * Stores the Store and the current fragment when the Activity is being stopped.
     * @param savedInstanceState the bundle to save state in.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_STORE, store);
        if (currentFragment != null && currentFragment.isAdded()){
            getSupportFragmentManager().putFragment(savedInstanceState, KEY_CURRENT_FRAGMENT, currentFragment);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (broadcastReceiver == null) {
            broadcastReceiver = new LocationBroadcastReceiver();
        }

        registerReceiver(broadcastReceiver, new IntentFilter(LOCATION_UPDATE));
    }

    /**
     * Inflates the action_bar.xml file with the App Bar.
     * @param menu the App Bar.
     * @return true when done inflating.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    /**
     * Invoked when the user press the Action bar.
     * @param item the MenuItem being pressed.
     * @return true then the choice was handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_reminder) {
            launchNewReminderFragment();
            return true;
        }

        if (id == R.id.list_reminders) {
            launchListFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a NewReminderFragment and starts a transaction with it.
     */
    @Override
    public void launchNewReminderFragment() {
        Fragment fragment = new NewReminderFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_fragment, fragment);
        transaction.commit();

        currentFragment = fragment;
    }

    /**
     * Creates a MyListFragment, attach the Reminders to it and start a transaction.
     */
    private void launchListFragment() {
        Fragment fragment = new MyListFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KEY_REMINDERS, store.getReminders());
        fragment.setArguments(bundle);
        transaction.replace(R.id.content_fragment, fragment);
        transaction.commit();
        currentFragment = fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * The last method in the Activity lifecycle. If the app is stopped
     * the Reminders has to be saved and the BroadCastReceiver need to disconnect.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     * Creates a new Reminder.
     * Also, if the CurrentLocationService has stopped, it will will be started.
     * @param l Location for the Reminder.
     * @param s Text for the Reminder.
     * @param n LocationName for the Reminder.
     * @param list List with further information of the Reminder.
     */
    @Override
    public void createReminder(Location l, String s, String n, ArrayList<String> list) {
        Reminder newReminder = new Reminder(l, s, n, Store.MAX_DISTANCE, list);
        if (store != null) {

            store.add(newReminder);

            if (serviceHasStopped(CurrentLocationService.class)) {
                startService(locationService);
            }
        }
        launchListFragment();
    }

    /**
     * Asks the system if a certain service is running.
     * @param serviceClass the class of the Service of interest.
     * @return true if serviceClass is running.
     */
    private boolean serviceHasStopped(Class<?> serviceClass) {
        ActivityManager activityMaager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo serviceInfo : activityMaager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the Reminder in Store located at a certain index.
     * @param index the index of the Reminder to be removed.
     */
    private void removeReminder(int index) {
        if (store != null) {
            store.remove(index);
            launchListFragment();
        }
    }

    /**
     * Constructs and sending a notification with the message String.
     * @param message The message to be displayed in the notification.
     */
    private void createNotification(String message) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(AppActivity.this)
                        .setSmallIcon(R.drawable.notification_yellow)
                        .setAutoCancel(true)
                        .setTicker("Ticker")
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("Reminder")
                        .setContentText(message)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_MAX)
                ;

        Intent resultIntent = new Intent(AppActivity.this, AppActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent pendingIntent =
                PendingIntent.getActivity(AppActivity.this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Stops the LocationSevice if there are no Reminders to keep track of,
     * and if there are Reminders in Store, and the LocationService is not running,
     * it starts it again.
     */
    @Override
    public void serviceControl() {
        if (store.isEmpty()) {
            stopService(locationService);

        } else {
            if (serviceHasStopped(CurrentLocationService.class)) {

                startService(locationService);
            }
        }
    }

    /**
     * Custom BroadCastReceiver for receiving messages from CurrentLocationService.
     */
    public class LocationBroadcastReceiver extends BroadcastReceiver {

        /**
         * Called when the receiver receives the intent.
         * Asking Store if the current location is near any of the Reminder's locations,
         * if so a notification is sent and the Reminder is removed.
         * @param context the context in which the receiver is running.
         * @param intent intent containing the current location.
         */
        @Override // when the receiver receives the intent
        public void onReceive(Context context, Intent intent) {

            Location location = intent.getParcelableExtra(LOCATION);
            int index = store.isNear(location);

            if (index >= 0) {
//                Toast.makeText(AppActivity.this, "Near!", Toast.LENGTH_SHORT).show();
                Reminder reminder = store.getIndex(index);
                createNotification(reminder.getText());
                removeReminder(index);
                serviceControl();
            }
        }
    }
}
