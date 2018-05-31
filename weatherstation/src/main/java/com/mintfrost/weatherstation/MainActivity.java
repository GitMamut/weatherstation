package com.mintfrost.weatherstation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LOCATION = "extraLocation";
    public static final String[] SERVICE_URL = new String[]{"currentOutdoor", "currentIndoor"};

    private static final String[] LOCATION_DESCRIPTION = new String[]{"OUTDOOR", "INDOOR"};
    private static final int NUMBER_OF_FRAGMENTS = 2;
    private static final TemperatureFragment[] FRAGMENTS_MAP = new TemperatureFragment[NUMBER_OF_FRAGMENTS];
    private static final String ACTION_CHECK_TEMPERATURE = "com.mintfrost.weatherstation.action.CHECK_TEMPERATURE";
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCurrentFragmentTemperature();
            }
        });
    }

    private void updateCurrentFragmentTemperature() {
        int currentItemId = mViewPager.getCurrentItem();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String endpointUrl = sharedPref.getString(SettingsActivity.PREFERENCE_ENDPOINT_URL, "");
        new FetchTemperatureTask(FRAGMENTS_MAP[currentItemId]).execute(endpointUrl, SERVICE_URL[currentItemId]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setChecked(isNotificationIntentEnabled());
        return true;
    }

    private boolean isNotificationIntentEnabled() {
        return getNotificationPendingIntent(PendingIntent.FLAG_NO_CREATE) != null;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_toggle_notification) {
            if (!item.isChecked()) {
                enableNotification(item);
            } else {
                disableNotification(item);
            }
            return true;
        }
        if (itemId == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableNotification(final MenuItem item) {
        Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                scheduleNotification(selectedHour, selectedMinute);
                item.setChecked(true);
            }
        }, hour, minute, true);
        mTimePicker.setTitle(getString(R.string.select_notification_time));
        mTimePicker.show();
    }

    private void disableNotification(MenuItem item) {
        Log.v(this.getClass().getCanonicalName(), "Trying to disable notification: " + ACTION_CHECK_TEMPERATURE);
        PendingIntent pendingIntent = getNotificationPendingIntent(PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Toast.makeText(MainActivity.this, R.string.disabled_notifications, Toast.LENGTH_LONG).show();
            Log.v(this.getClass().getCanonicalName(), "Disabled notification: " + ACTION_CHECK_TEMPERATURE);
        } else {
            Log.v(this.getClass().getCanonicalName(), "Notification not found: " + ACTION_CHECK_TEMPERATURE);
        }
        item.setChecked(false);
    }

    private PendingIntent getNotificationPendingIntent(int creationFlag) {
        Intent notificationIntent = new Intent(ACTION_CHECK_TEMPERATURE, null, MainActivity.this, NotificationPublisher.class);
        return PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, creationFlag);
    }

    private void scheduleNotification(int selectedHour, int selectedMinute) {
        PendingIntent pendingIntent = getNotificationPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        String setNotificationText =
                String.format(getString(R.string.enabled_notifications_at), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        Toast.makeText(MainActivity.this, setNotificationText, Toast.LENGTH_LONG).show();
        Log.v(this.getClass().getCanonicalName(), setNotificationText + ": " + ACTION_CHECK_TEMPERATURE);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            TemperatureFragment temperatureFragment = new TemperatureFragment();

            Bundle bdl = new Bundle(1);
            bdl.putString(EXTRA_LOCATION, LOCATION_DESCRIPTION[position]);
            temperatureFragment.setArguments(bdl);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String endpointUrl = sharedPref.getString(SettingsActivity.PREFERENCE_ENDPOINT_URL, "");
            new FetchTemperatureTask(temperatureFragment).execute(endpointUrl, SERVICE_URL[position]);

            return temperatureFragment;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            FRAGMENTS_MAP[position] = (TemperatureFragment) createdFragment;

            return createdFragment;
        }

        @Override
        public int getCount() {
            return NUMBER_OF_FRAGMENTS;
        }
    }

}
