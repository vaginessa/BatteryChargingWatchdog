package ch.ebhardjan.batterychargingwatchdog;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jan on 09.02.16.
 *
 *
 */
public class PowerConnectionReceiver extends BroadcastReceiver {

    protected static final String TAG = "###BroadcastReceiver";
    private Timer timer;

    private void createNotification(Context ctx, String txt){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText(txt);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(ctx, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(666, mBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        SharedPreferences sp = context.getSharedPreferences(Config.sharedPrefs, Context.MODE_PRIVATE);
        boolean active = sp.getBoolean(Config.active, false);
        createNotification(context, "active:"+active);
        if(active){
            if(timer == null)
                timer = new Timer();
            else
                timer.cancel();
            timer.scheduleAtFixedRate(new PeriodicalTask(context), 0, Config.ChargingSamplingInterval);
        }
    }

    private class PeriodicalTask extends TimerTask {

        private Context ctx;

        PeriodicalTask (Context ctx){
            this.ctx = ctx;
        }

        @Override
        public void run() {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = ctx.registerReceiver(null, ifilter);
            if(batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPercentage = level / (float) scale;
                Log.d(TAG, "Periodical Task");
                Log.d(TAG, "batteryPercentage: "+batteryPercentage);
                createNotification(batteryPercentage);
            }
            else
                Log.d(TAG, "Couldn't get the battery level...");
        }

        private void createNotification(float bP){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("My notification")
                            .setContentText("Percentage: "+bP);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(ctx, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(666, mBuilder.build());
        }
    }
}
