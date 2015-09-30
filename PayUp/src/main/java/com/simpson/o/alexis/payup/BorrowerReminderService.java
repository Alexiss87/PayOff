package com.simpson.o.alexis.payup;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;



/**
 * Created by Alexis on 6/11/2014.
 */
public class BorrowerReminderService extends WakeReminderIntentService {

    private static final String TAG = "com.simpson.o.alexis.payup.BorrowerReminderService";
    private static final int REMINDER_INTERVAL = 1000 * 60*2; // 1 minutes
    private SharedPreferences sharedPrefs;
    int numMessages = 0;
    boolean firstTime = true;

    public BorrowerReminderService() {
        super(TAG);
    }
    @Override
    void doReminderWork(Intent intent) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean soundOn = sharedPrefs.getBoolean("KEY_ALERT_SOUND",false);
        Boolean vibrate = sharedPrefs.getBoolean("KEY_VIBRATE_ON_ALERT",false);
        String soundUriString =null;

      //  String type = intent.getStringExtra("TAG");
      //  Toast.makeText(this, "doing remider work for borrower", Toast.LENGTH_SHORT).show();

            Long borrowerId = intent.getExtras()
                    .getLong(BorrowersDetailFragment.EXTRA_BORROWER_ID);

            // Status bar notification Code Goes here.
            Borrower borrower = LoanOffice.getInstance(this).getBorrower(borrowerId);
            Intent i = new Intent(this, BorrowersDetailActivity.class);
            i.putExtra(BorrowersDetailFragment.EXTRA_BORROWER_ID, borrowerId);
            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
             TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
             stackBuilder.addParentStack(BorrowersDetailActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
             stackBuilder.addNextIntent(i);
             PendingIntent pi =stackBuilder.getPendingIntent(0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

            /*PendingIntent pi = PendingIntent
                    .getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);*/

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(getResources().getString(R.string.borrower_due))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(borrower.getName())
                    .setContentText((getResources().getString(R.string.borrower_context_msg)))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();

        if (soundOn){
            //playDefaultNotificationSound();
            soundUriString =sharedPrefs.getString("KEY_RINGTONE_PREFS", "DEFAULT_SOUND");
            notification.sound = Uri.parse(soundUriString);
        }
        if (vibrate){
            notification.vibrate = new long[] { 1000, 1000,};
        }

            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            int id = (int) ((long) borrowerId);
            notificationManager.notify(id, notification);


    }

}


