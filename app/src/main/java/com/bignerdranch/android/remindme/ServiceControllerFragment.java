package com.bignerdranch.android.remindme;

import android.app.Activity;

/**
 * Created by annika on 2017-08-18.
 */

/**
 * Both MyListFragment and NewReminderFragment need to call the same method serviceControl() in
 * AppActivity, therefore they inherit from this class to avoid duplicate code.
 */
public abstract class ServiceControllerFragment extends android.support.v4.app.Fragment {

    protected ServiceController serviceController;

    protected interface ServiceController {
        void serviceControl();
    }

    /**
     * Makes sure that the Activity starting the Fragment is implementing ServiceController.
     * @param a the calling activity.
     */
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);

        try {
            serviceController = (ServiceController) a;

        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " does not implement ServiceController interface");
        }
    }

}
