package com.bignerdranch.android.remindme;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
/**
 * MyRecyclerAdapter is a customized RecyclerAdapter for displaying the reminders with the text and
 * the location name. It also provides the user with options for each reminder, such as edit text,
 * change Place and remove.
 * It communicates with MyListFragment through the UserInputDelegate interface.
 */
class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ReminderHolder> {

    private ArrayList<Reminder> dataset;
    private Context context;
    private UserInputDelegate delegate;

    MyRecyclerAdapter(ArrayList<Reminder> reminders, Context c, UserInputDelegate d) {
        dataset = reminders;
        context = c;
        delegate = d;
    }

    /**
     * Interface implemented by MyListFragment, for handling button clicks
     * in the options menu.
     */
    interface UserInputDelegate {
        void pickNewPlace(ReminderHolder holder);
        void editText(ReminderHolder r);
        void notifyActivity();
        void setButtonVisibility();
        void initializeRecyclerView();
    }

    /**
     * Inflates the ReminderHolder with the view.
     * @param parent the parent in which the new view will be added to.
     * @param viewType view type of the new view.
     * @return a ViewHolder with the new View.
     */
    @Override
    public ReminderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ReminderListItemView mapViewListItemView = new ReminderListItemView(context);
        mapViewListItemView.mapViewOnCreate(null);

        return new ReminderHolder(mapViewListItemView);
    }

    /**
     * The RecyclerView calls it to display the data at the specified position.
     * @param holder the current reminderholder.
     * @param position the specified position.
     */
    @Override
    public void onBindViewHolder(ReminderHolder holder, int position) {
        final Reminder reminder = dataset.get(position);
        ReminderHolder reminderHolder = (ReminderHolder) holder;

        holder.setReminder(reminder);
        reminderHolder.mapViewListItemViewOnResume();

        holder.setTitleView(reminder.getText());
        holder.setLocationView(reminder.getLocationName());
//        holder.setReminderListView(reminder.listToString());
        generateCheckList(reminder);
        setButtonListener(holder);
        holder.position = position;
        holder.setMapVisibility();
    }

    private void generateCheckList(Reminder reminder) {
        ArrayList<String> list = reminder.getList();


    }

    /**
     * Creates the optionsButton and sets the onclickListener to it.
     * When clicked, a little options menu pops up, where the user
     * can choose to remove the reminder, edit the text or change the place.
     * @param holder
     */
    private void setButtonListener(ReminderHolder holder) {
        final Context c = this.context;
        final ReminderHolder reminderHolder = holder;
        final ArrayList<Reminder> reminderArrayList = dataset;

        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(c, reminderHolder.getOptionsButton());
                popupMenu.inflate(R.menu.reminder_options_menu);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.options_menu_edit_location:
                                delegate.pickNewPlace(reminderHolder);
                                break;

                            case R.id.options_menu_edit_text:
                                delegate.editText(reminderHolder);


                                notifyItemChanged(reminderHolder.position);
                                break;

                            case R.id.options_menu_delete:
                                reminderArrayList.remove(reminderHolder.reminder);
                                notifyDataSetChanged();
                                delegate.setButtonVisibility();
                                delegate.initializeRecyclerView();
                                delegate.notifyActivity();
                                break;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        };

        holder.setOptionsButton(buttonListener);
    }

    /**
     * @return the number of reminders held by the adapter.
     */
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    /**
     * ReminderHolder is a customized ViewHolder,
     * that binds the data of the Reminder to a view for being displayed
     * inside the RecyclerView.
     */
    class ReminderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ReminderListItemView reminderListItemView;
        Reminder reminder;
        int position;

        ReminderHolder(ReminderListItemView view) {
            super(view);
            reminderListItemView = view;
            reminderListItemView.setOnClickListener(this);
        }

        /**
         * For the user to fold and unfold the map by pressing on the menu item.
         * The visible state is held inside the Reminder class.
         */
        void setMapVisibility() {
            if (reminder.isMapIsVisible()) {
                reminderListItemView.mapView.setVisibility(View.VISIBLE);
            } else {
                reminderListItemView.mapView.setVisibility(View.GONE);
            }
        }

        /**
         * Sets the title of the Reminder in the ReminderListItemView's
         * corresponding TextView.
         * @param text the title of the reminder.
         */
        void setTitleView(String text) {
            reminderListItemView.setTitleText(text);
        }

        /**
         * Sets the name of the location of the Reminder in the ReminderListItemView's
         * corresponding TextView.
         * @param text the location name of the reminder.
         */
        void setLocationView(String text) {
            reminderListItemView.setLocationNameText(text);
        }

        /**
         * Sets the list of the Reminder in the ReminderListItemView's
         * corresponding TextView.
         * @param text the list of the reminder.
         */
        void setReminderListView(String text) {
            reminderListItemView.setListText(text);
        }

        /**
         * Sets MyRecyclerAdapter's OnClickListener to the optionsButton.
         * @param listener OnClickListener from the MyRecyclerAdapter.
         */
        void setOptionsButton(View.OnClickListener listener) {
            if (reminderListItemView.getOptionsButton() != null) {
                reminderListItemView.getOptionsButton().setOnClickListener(listener);
            }
        }

        /**
         * The PopupMenu in optionsButton's OnClickListener needs
         * this reference to set it's context.
         * @return the optionsButton.
         */
        ImageButton getOptionsButton() {
            return reminderListItemView.getOptionsButton();
        }

        /**
         * Binds the Reminder object to the ReminderListItemView.
         * @param reminder
         */
        void setReminder(Reminder reminder) {
            this.reminder = reminder;
            reminderListItemView.setReminder(reminder);
        }

        /**
         * It is mandatory to forward the lifecycle events from the hosting fragment
         * to the ReminderListItemView, or else they may get out of sync.
         */
        void mapViewListItemViewOnResume() {
            if (reminderListItemView != null) {
                reminderListItemView.mapViewOnResume();
            }
        }

        /**
         * Toggle the visibility of the map.
         * @param view the rootView.
         */
        @Override
        public void onClick(View view) {
            reminderListItemView.mapView.setVisibility(reminder.isMapIsVisible()
                    ? View.GONE
                    : View.VISIBLE);

            reminder.toggleMapVisible();
        }
    }
}


///**
// * Created by annika on 2017-08-16.
// */
//
//
///**
// * MyRecyclerAdapter is a customized RecyclerAdapter for displaying the reminders with the text and
// * the location name. It also provides the user with options for each reminder, such as edit text,
// * change Place and remove.
// * It communicates with MyListFragment through the UserInputDelegate interface.
// */
//class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ReminderHolder> {
//
//    private ArrayList<Reminder> dataset;
//    private Context context;
//    private UserInputDelegate delegate;
//
//    MyRecyclerAdapter(ArrayList<Reminder> reminders, Context c, UserInputDelegate d) {
//        dataset = reminders;
//        context = c;
//        delegate = d;
//    }
//
//    /**
//     * Interface implemented by MyListFragment, for handling button clicks
//     * in the options menu.
//     */
//    interface UserInputDelegate {
//        void pickNewPlace(ReminderHolder holder);
//        void editText(ReminderHolder r);
//        void notifyActivity();
//        void setButtonVisibility();
//        void initializeRecyclerView();
//    }
//
//    /**
//     * Inflates the ReminderHolder with the view.
//     * @param parent the parent in which the new view will be added to.
//     * @param viewType view type of the new view.
//     * @return a ViewHolder with the new View.
//     */
//    @Override
//    public ReminderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        ReminderListItemView mapViewListItemView = new ReminderListItemView(context);
//        mapViewListItemView.mapViewOnCreate(null);
//
//        return new ReminderHolder(mapViewListItemView);
//    }
//
//    /**
//     * The RecyclerView calls it to display the data at the specified position.
//     * @param holder the current reminderholder.
//     * @param position the specified position.
//     */
//    @Override
//    public void onBindViewHolder(ReminderHolder holder, int position) {
//        final Reminder reminder = dataset.get(position);
//        ReminderHolder reminderHolder = (ReminderHolder) holder;
//
//        holder.setReminder(reminder);
//        reminderHolder.mapViewListItemViewOnResume();
//
//        holder.setTitleView(reminder.getText());
//        holder.setLocationView(reminder.getLocationName());
//        setButtonListener(holder);
//        holder.position = position;
//        holder.setMapVisibility();
//    }
//
//    /**
//     * Creates the optionsButton and sets the onclickListener to it.
//     * When clicked, a little options menu pops up, where the user
//     * can choose to remove the reminder, edit the text or change the place.
//     * @param holder
//     */
//    private void setButtonListener(ReminderHolder holder) {
//        final Context c = this.context;
//        final ReminderHolder reminderHolder = holder;
//        final ArrayList<Reminder> reminderArrayList = dataset;
//
//        View.OnClickListener buttonListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PopupMenu popupMenu = new PopupMenu(c, reminderHolder.getOptionsButton());
//                popupMenu.inflate(R.menu.reminder_options_menu);
//
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//
//                            case R.id.options_menu_edit_location:
//                                delegate.pickNewPlace(reminderHolder);
//                                break;
//
//                            case R.id.options_menu_edit_text:
//                                delegate.editText(reminderHolder);
//
//
//                                notifyItemChanged(reminderHolder.position);
//                                break;
//
//                            case R.id.options_menu_delete:
//                                reminderArrayList.remove(reminderHolder.reminder);
//                                notifyDataSetChanged();
//                                delegate.setButtonVisibility();
//                                delegate.initializeRecyclerView();
//                                delegate.notifyActivity();
//                                break;
//                        }
//                        return false;
//                    }
//                });
//
//                popupMenu.show();
//            }
//        };
//
//        holder.setOptionsButton(buttonListener);
//    }
//
//    /**
//     * @return the number of reminders held by the adapter.
//     */
//    @Override
//    public int getItemCount() {
//        return dataset.size();
//    }
//
//    /**
//     * ReminderHolder is a customized ViewHolder,
//     * that binds the data of the Reminder to a view for being displayed
//     * inside the RecyclerView.
//     */
//    class ReminderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        private ReminderListItemView reminderListItemView;
//        Reminder reminder;
//        int position;
//
//        ReminderHolder(ReminderListItemView view) {
//            super(view);
//            reminderListItemView = view;
//            reminderListItemView.setOnClickListener(this);
//        }
//
//        /**
//         * For the user to fold and unfold the map by pressing on the menu item.
//         * The visible state is held inside the Reminder class.
//         */
//        void setMapVisibility() {
//            if (reminder.isMapIsVisible()) {
//                reminderListItemView.mapView.setVisibility(View.VISIBLE);
//            } else {
//                reminderListItemView.mapView.setVisibility(View.GONE);
//            }
//        }
//
////        void expand(final View v) {
////            v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
////            final int targetHeight = v.getMeasuredHeight();
////
//////            final int targetHeight = v.getHeight();
////
////
////            System.out.println("PRE: " + v.getLayoutParams().height);
////
////            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
////            v.getLayoutParams().height = 1;
////            v.setVisibility(View.VISIBLE);
////            System.out.println("PRO: " + v.getLayoutParams().height);
////            Animation a = new Animation() {
////                @Override
////                protected void applyTransformation(float interpolatedTime, Transformation t) {
////                    v.getLayoutParams().height = interpolatedTime == 1
////                            ? RecyclerView.LayoutParams.WRAP_CONTENT
////                            : (int) (targetHeight * interpolatedTime);
////                    v.requestLayout();
////                }
////
////                @Override
////                public boolean willChangeBounds() {
////                    return true;
////                }
////            };
////
////            // 1dp/ms
////            a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
////            v.startAnimation(a);
//////            reminderListItemView.mapView.setVisibility(View.VISIBLE);
////        }
////
////        void collapse(final View v) {
////            final int initialHeight = v.getMeasuredHeight();
////
////            Animation a = new Animation() {
////                @Override
////                protected void applyTransformation(float interpolatedTime, Transformation t) {
////                    if (interpolatedTime == 1) {
////                        v.setVisibility(View.GONE);
////                    }else{
////                        v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
////                        v.requestLayout();
////                    }
////                }
////
////                @Override
////                public boolean willChangeBounds() {
////                    return true;
////                }
////            };
////
////            // 1dp/ms
////            a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
////            v.startAnimation(a);
//////            reminderListItemView.mapView.setVisibility(View.GONE);
////        }
//
//        /**
//         * Sets the title of the Reminder in the ReminderListItemView's
//         * corresponding TextView.
//         * @param text the title of the reminder.
//         */
//        void setTitleView(String text) {
//            reminderListItemView.setTitleText(text);
//        }
//
//        /**
//         * Sets the name of the location of the Reminder in the ReminderListItemView's
//         * corresponding TextView.
//         * @param text the location name of the reminder.
//         */
//        void setLocationView(String text) {
//            reminderListItemView.setLocationNameText(text);
//        }
//
//        /**
//         * Sets MyRecyclerAdapter's OnClickListener to the optionsButton.
//         * @param listener OnClickListener from the MyRecyclerAdapter.
//         */
//        void setOptionsButton(View.OnClickListener listener) {
//            if (reminderListItemView.getOptionsButton() != null) {
//                reminderListItemView.getOptionsButton().setOnClickListener(listener);
//            }
//        }
//
//        /**
//         * The PopupMenu in optionsButton's OnClickListener needs
//         * this reference to set it's context.
//         * @return the optionsButton.
//         */
//        ImageButton getOptionsButton() {
//            return reminderListItemView.getOptionsButton();
//        }
//
//        /**
//         * Binds the Reminder object to the ReminderListItemView.
//         * @param reminder
//         */
//        void setReminder(Reminder reminder) {
//            this.reminder = reminder;
//            reminderListItemView.setReminder(reminder);
//        }
//
//        /**
//         * It is mandatory to forward the lifecycle events from the hosting fragment
//         * to the ReminderListItemView, or else they may get out of sync.
//         */
//        void mapViewListItemViewOnResume() {
//            if (reminderListItemView != null) {
//                reminderListItemView.mapViewOnResume();
//            }
//        }
//
//        /**
//         * Toggle the visibility of the map.
//         * @param view the rootView.
//         */
//        @Override
//        public void onClick(View view) {
//            reminderListItemView.mapView.setVisibility(reminder.isMapIsVisible()
//                    ? View.GONE
//                    : View.VISIBLE);
////
////            if (reminder.isMapIsVisible()) {
////                collapse(reminderListItemView.mapView);
////            } else {
////                expand(reminderListItemView.mapView);
////            }
//
//            reminder.toggleMapVisible();
//        }
//    }
//}