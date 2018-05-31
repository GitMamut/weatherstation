package com.mintfrost.weatherstation;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

public class NotificationPublisher extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.v(this.getClass().getCanonicalName(), "Received alarm broadcast on action: " + intent.getAction());
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            // Set the alarm here.
        } else {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String endpointUrl = sharedPref.getString(SettingsActivity.PREFERENCE_ENDPOINT_URL, "");
            new FetchTemperatureTask(new NotificationListener(context)).execute(endpointUrl, MainActivity.SERVICE_URL[0]);
        }
    }

    public class NotificationListener implements DateFetchListener {

        private Context context;

        public NotificationListener(Context context) {
            this.context = context;
        }

        @Override
        public void notifyStart() {
            //do nothing
        }

        @Override
        public void notifyComplete(List<ConditionSnapshot> result) {
            if (result.size() > 0) {
                dispatchNotification(result.get(0));
            }
        }

        @Override
        public void notifyError(String errorReason) {
            // do nothing
        }

        private void dispatchNotification(ConditionSnapshot conditionSnapshot) {
            String CHANNEL_ID = "my_channel_01";
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_home_temperature)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                            .setContentTitle("Current weatherstation outdoor: " + conditionSnapshot.getTempValue() + TemperatureFragment.DEGREES_CELSIUS)
                            .setContentText(conditionSnapshot.getDate());

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int mNotificationId = 8;
            mNotificationManager.notify(mNotificationId, mBuilder.build());
        }
    }
}
